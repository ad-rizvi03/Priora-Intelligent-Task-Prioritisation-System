package com.gravitycanvas.service;

import com.gravitycanvas.dto.*;
import com.gravitycanvas.entity.ApprovalStatus;
import com.gravitycanvas.entity.Task;
import com.gravitycanvas.entity.TaskStatus;
import com.gravitycanvas.entity.UserAccount;
import com.gravitycanvas.repository.TaskRepository;
import com.gravitycanvas.repository.UserAccountRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service encapsulating business operations for Tasks, Gravity Score calculations,
 * and Developer Ticket Manager Authorization workflows.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserAccountRepository userRepository;
    private final GravityCalculatorService gravityCalculatorService;
    private final SettingsService settingsService;

    public TaskService(TaskRepository taskRepository,
                       UserAccountRepository userRepository,
                       GravityCalculatorService gravityCalculatorService,
                       SettingsService settingsService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.gravityCalculatorService = gravityCalculatorService;
        this.settingsService = settingsService;
    }

    /**
     * Fetches all APPROVED tasks formatted with dynamic Gravity Scores for display on the main canvas.
     */
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getAllTasks() {
        List<Task> allTasks = taskRepository.findAll();
        List<TaskResponseDto> dtos = allTasks.stream()
            .filter(task -> task.getApprovalStatus() == ApprovalStatus.APPROVED)
            .map(task -> gravityCalculatorService.calculateGravity(task, allTasks))
            .sorted(Comparator.comparingDouble(TaskResponseDto::getTotalGravityScore).reversed())
            .collect(Collectors.toList());

        // Identify The Domino of the Day (Single highest-leverage active keystone ticket)
        TaskResponseDto dominoTask = dtos.stream()
            .filter(d -> d.getStatus() != TaskStatus.DONE && d.getDominoLeverageScore() > 0)
            .max(Comparator.comparingDouble(TaskResponseDto::getDominoLeverageScore))
            .orElse(null);

        // Fallback: If no dependencies are defined yet, designate top Focus bottleneck task
        if (dominoTask == null) {
            dominoTask = dtos.stream()
                .filter(d -> d.getStatus() != TaskStatus.DONE && "FOCUS".equalsIgnoreCase(d.getCanvasZone()))
                .max(Comparator.comparingDouble(TaskResponseDto::getTotalGravityScore))
                .orElse(null);
        }

        if (dominoTask != null) {
            dominoTask.setDominoOfDay(true);
        }

        return dtos;
    }

    /**
     * Fetches all tasks awaiting Manager Approval (Manager Queue).
     */
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getPendingTasks() {
        List<Task> allTasks = taskRepository.findAll();
        return allTasks.stream()
            .filter(task -> task.getApprovalStatus() == ApprovalStatus.PENDING_APPROVAL)
            .map(task -> gravityCalculatorService.calculateGravity(task, allTasks))
            .sorted(Comparator.comparing(TaskResponseDto::getCreatedAt).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Creates a new task card (Open to both Managers and Developers).
     * If created by a Developer and Manager settings require authorization (requireApprovalForDevTasks == true),
     * the task is saved with PENDING_APPROVAL status.
     */
    @Transactional
    public TaskResponseDto createTask(CreateTaskRequest req) {
        Task task = new Task();
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setEnvironment(req.getEnvironment());
        task.setCustomFactorApplied(req.isCustomFactorApplied());
        task.setDeadline(req.getDeadline());
        task.setCreatedAt(LocalDateTime.now());
        task.setLastActivityAt(LocalDateTime.now());

        // Get Current Authenticated User (Creator)
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAccount creator = userRepository.findByUsername(currentUsername).orElse(null);
        task.setCreatedBy(creator);

        // Assignee
        if (req.getAssigneeId() != null) {
            UserAccount assignee = userRepository.findById(req.getAssigneeId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + req.getAssigneeId()));
            checkCircuitBreaker(assignee, req.isOverrideCircuitBreaker());
            task.setAssignee(assignee);
        } else {
            checkCircuitBreaker(creator, req.isOverrideCircuitBreaker());
            task.setAssignee(creator);
        }

        // Parent Dependencies
        if (req.getParentTaskIds() != null && !req.getParentTaskIds().isEmpty()) {
            Set<Task> parentTasks = new HashSet<>(taskRepository.findAllById(req.getParentTaskIds()));
            task.setDependencies(parentTasks);
            for (Task parent : parentTasks) {
                parent.getDependentTasks().add(task);
            }
        }

        // All manager/admin created tasks are directly APPROVED
        task.setApprovalStatus(ApprovalStatus.APPROVED);

        Task saved = taskRepository.save(task);
        return gravityCalculatorService.calculateGravity(saved, taskRepository.findAll());
    }

    /**
     * Manager authorizes and approves a pending developer task.
     */
    @Transactional
    public TaskResponseDto approveTask(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));

        task.setApprovalStatus(ApprovalStatus.APPROVED);
        task.setLastActivityAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);
        return gravityCalculatorService.calculateGravity(saved, taskRepository.findAll());
    }

    /**
     * Manager rejects a pending developer task with mandatory reason.
     */
    @Transactional
    public TaskResponseDto rejectTask(Long id, RejectTaskRequest req) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));

        task.setApprovalStatus(ApprovalStatus.REJECTED);
        if (req != null && req.getRejectionReason() != null && !req.getRejectionReason().trim().isEmpty()) {
            task.setRejectionReason(req.getRejectionReason().trim());
        } else {
            task.setRejectionReason("No rejection reason provided by manager.");
        }
        task.setLastActivityAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);
        return gravityCalculatorService.calculateGravity(saved, taskRepository.findAll());
    }

    /**
     * Deletes a task by ID (Manager permission required).
     */
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new IllegalArgumentException("Task not found with ID: " + id);
        }
        taskRepository.deleteById(id);
    }

    @Transactional
    public TaskResponseDto updateDeadline(Long id, UpdateDeadlineRequest req) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));

        task.setDeadline(req.getDeadline());
        task.setLastActivityAt(LocalDateTime.now());
        Task updated = taskRepository.save(task);
        return gravityCalculatorService.calculateGravity(updated, taskRepository.findAll());
    }

    @Transactional
    public TaskResponseDto reassignTask(Long id, ReassignTaskRequest req) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));

        if (req.getAssigneeId() != null) {
            UserAccount assignee = userRepository.findById(req.getAssigneeId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + req.getAssigneeId()));
            checkCircuitBreaker(assignee, req.isOverrideCircuitBreaker());
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }

        task.setLastActivityAt(LocalDateTime.now());
        Task updated = taskRepository.save(task);
        return gravityCalculatorService.calculateGravity(updated, taskRepository.findAll());
    }

    @Transactional
    public TaskResponseDto updateStatus(Long id, UpdateStatusRequest req) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));

        task.setStatus(req.getStatus());
        task.setLastActivityAt(LocalDateTime.now());
        Task updated = taskRepository.save(task);
        return gravityCalculatorService.calculateGravity(updated, taskRepository.findAll());
    }

    /**
     * Toggle Ping Blocker State (ON/OFF) and update notification alert state.
     */
    @Transactional
    public TaskResponseDto pingBlocker(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));

        boolean newPingedState = !task.isPinged();
        task.setPinged(newPingedState);
        if (newPingedState) {
            task.setPingCount(task.getPingCount() + 1);
        }
        task.setLastActivityAt(LocalDateTime.now());
        Task updated = taskRepository.save(task);
        return gravityCalculatorService.calculateGravity(updated, taskRepository.findAll());
    }

    /**
     * Enforces the Gravitational Burnout Circuit Breaker.
     * Prevents assigning tasks to developers whose active workload exceeds safe event horizon thresholds.
     */
    private void checkCircuitBreaker(UserAccount assignee, boolean override) {
        if (override || assignee == null) {
            return;
        }

        List<Task> allTasks = taskRepository.findAll();
        List<Task> activeAssigned = allTasks.stream()
            .filter(t -> t.getAssignee() != null && t.getAssignee().getId().equals(assignee.getId()) && t.getStatus() != TaskStatus.DONE)
            .collect(Collectors.toList());

        double totalLoad = 0.0;
        int focusCount = 0;
        for (Task t : activeAssigned) {
            TaskResponseDto taskDto = gravityCalculatorService.calculateGravity(t, allTasks);
            totalLoad += taskDto.getTotalGravityScore();
            if (taskDto.getCanvasZone() != null && "FOCUS".equalsIgnoreCase(taskDto.getCanvasZone())) {
                focusCount++;
            }
        }

        if (totalLoad >= 200.0 || focusCount >= 3) {
            double capacityPct = Math.round((totalLoad / 200.0) * 1000.0) / 10.0;
            throw new IllegalArgumentException(
                "Gravitational Circuit Breaker Active: " + assignee.getFullName() + " is at " + capacityPct + 
                "% capacity (" + Math.round(totalLoad) + " pts in Supernova Lock with " + focusCount + 
                " critical Focus tasks). Task assignment is blocked to prevent burnout and delivery failure. " +
                "Select an alternative team member or enable 'Emergency Override'."
            );
        }
    }

    /**
     * Feature 3: The 1-Click Ticket Defibrillator.
     * Takes decisive action against stagnating and zombie blocker tickets:
     * - "SPLIT": Extracts immediate MVP unblocker as Phase 1 and defers Phase 2.
     * - "SHED_GRAVITY": Resets pings, drops custom weight, and extends deadline by 7 days.
     * - "SMART_PASS": Automatically reassigns to the team member with the lowest active gravity load.
     * - "DISSOLVE": Gracefully resolves/closes the blocker ticket, releasing all trapped gravity.
     */
    @Transactional
    public TaskResponseDto defibrillateTask(Long id, DefibrillateTaskRequest req) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));

        String action = (req != null && req.getAction() != null) ? req.getAction().trim().toUpperCase() : "SHED_GRAVITY";

        switch (action) {
            case "SPLIT": {
                String originalTitle = task.getTitle();
                task.setTitle("[Phase 1 Unblocker] " + originalTitle);
                task.setDescription("Critical unblocking scope split from parent ticket. " + 
                    (task.getDescription() != null ? task.getDescription() : ""));
                task.setPinged(false);
                task.setPingCount(0);
                task.setLastActivityAt(LocalDateTime.now());
                taskRepository.save(task);

                // Spawn Phase 2 Follow-Up
                Task phase2 = new Task();
                phase2.setTitle("[Phase 2 Scope] " + originalTitle);
                phase2.setDescription("Deferred non-blocking enhancements from: #" + task.getId());
                phase2.setEnvironment(task.getEnvironment());
                phase2.setApprovalStatus(ApprovalStatus.APPROVED);
                phase2.setStatus(TaskStatus.TODO);
                phase2.setAssignee(task.getAssignee());
                phase2.setCreatedBy(task.getCreatedBy());
                phase2.setCreatedAt(LocalDateTime.now());
                phase2.setLastActivityAt(LocalDateTime.now());
                phase2.setDeadline(task.getDeadline() != null ? task.getDeadline().plusDays(5) : LocalDateTime.now().plusDays(7));
                phase2.setPinged(false);
                phase2.setPingCount(0);
                taskRepository.save(phase2);
                break;
            }

            case "SHED_GRAVITY": {
                task.setPinged(false);
                task.setPingCount(0);
                task.setCustomFactorApplied(false);
                if (task.getDeadline() != null) {
                    task.setDeadline(task.getDeadline().plusDays(7));
                } else {
                    task.setDeadline(LocalDateTime.now().plusDays(7));
                }
                task.setLastActivityAt(LocalDateTime.now());
                taskRepository.save(task);
                break;
            }

            case "SMART_PASS": {
                UserAccount targetAssignee = null;
                if (req != null && req.getTargetAssigneeId() != null) {
                    targetAssignee = userRepository.findById(req.getTargetAssigneeId()).orElse(null);
                }
                if (targetAssignee == null) {
                    List<UserAccount> members = userRepository.findAll();
                    List<Task> all = taskRepository.findAll();
                    UserAccount bestCandidate = null;
                    double minLoad = Double.MAX_VALUE;

                    for (UserAccount u : members) {
                        double userLoad = all.stream()
                            .filter(t -> t.getAssignee() != null && t.getAssignee().getId().equals(u.getId()) && t.getStatus() != TaskStatus.DONE)
                            .mapToDouble(t -> gravityCalculatorService.calculateGravity(t, all).getTotalGravityScore())
                            .sum();
                        if (userLoad < minLoad) {
                            minLoad = userLoad;
                            bestCandidate = u;
                        }
                    }
                    targetAssignee = bestCandidate;
                }

                if (targetAssignee != null) {
                    task.setAssignee(targetAssignee);
                }
                task.setPinged(false);
                task.setPingCount(0);
                task.setLastActivityAt(LocalDateTime.now());
                taskRepository.save(task);
                break;
            }

            case "DISSOLVE": {
                task.setStatus(TaskStatus.DONE);
                task.setPinged(false);
                task.setPingCount(0);
                task.setLastActivityAt(LocalDateTime.now());
                taskRepository.save(task);
                break;
            }

            default:
                throw new IllegalArgumentException("Unknown defibrillator action: " + action);
        }

        return gravityCalculatorService.calculateGravity(task, taskRepository.findAll());
    }

    /**
     * Feature 4: What-If Sprint Time Machine & Cascade Simulator.
     * Simulates deadline slips and engineer capacity shocks forward in time,
     * calculating failure risks, cascade paths, and recommended mitigation plans.
     */
    @Transactional(readOnly = true)
    public SprintSimulationResponse simulateSprintScenario(SprintSimulationRequest req) {
        List<Task> allTasks = taskRepository.findAll();
        Task targetTask = null;
        if (req != null && req.getDelayedTaskId() != null) {
            targetTask = taskRepository.findById(req.getDelayedTaskId()).orElse(null);
        }
        if (targetTask == null) {
            targetTask = allTasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE)
                .max(Comparator.comparingDouble(t -> gravityCalculatorService.calculateGravity(t, allTasks).getTotalGravityScore()))
                .orElse(null);
        }

        SprintSimulationResponse res = new SprintSimulationResponse();
        if (targetTask == null) {
            res.setProjectedFailureRisk(0.0);
            res.setRiskLevel("NOMINAL");
            res.setRecommendedMitigationPlan("No active tasks found to simulate.");
            return res;
        }

        int delayHours = (req != null && req.getDelayHours() > 0) ? req.getDelayHours() : 24;
        LocalDateTime simulatedFinish = (targetTask.getDeadline() != null ? targetTask.getDeadline() : LocalDateTime.now()).plusHours(delayHours);

        res.getCascadeSteps().add("⏱️ [Primary Delay Event] Task #" + targetTask.getId() + " (" + targetTask.getTitle() + 
            ") projected to finish at " + simulatedFinish.toString().replace('T', ' ') + " (+" + delayHours + "h delay).");

        Set<Task> downstream = gravityCalculatorService.getAllDownstreamTasks(targetTask);
        int overdueCount = 0;
        double dragIncrease = 0.0;
        Task criticalDownstreamTask = null;

        for (Task dep : downstream) {
            if (dep.getStatus() == TaskStatus.DONE) continue;
            LocalDateTime depDeadline = dep.getDeadline() != null ? dep.getDeadline() : LocalDateTime.now().plusHours(48);
            if (simulatedFinish.isAfter(depDeadline)) {
                overdueCount++;
                long breachHours = java.time.Duration.between(depDeadline, simulatedFinish).toHours();
                String assigneeName = dep.getAssignee() != null ? dep.getAssignee().getFullName() : "Unassigned";
                res.getCascadeSteps().add("💥 [Downstream Breach] Task #" + dep.getId() + " (" + dep.getTitle() + 
                    ") is delayed past its deadline by " + breachHours + " hours! Blocked assignee: " + assigneeName + ".");
                dragIncrease += 35.0 + (breachHours * 0.5);
                if (criticalDownstreamTask == null) {
                    criticalDownstreamTask = dep;
                }
            } else {
                res.getCascadeSteps().add("⏳ [Buffered Impact] Task #" + dep.getId() + " is delayed but remains within deadline safety buffer.");
                dragIncrease += 15.0;
            }
        }

        // Check if an engineer was taken offline / unavailable
        if (req != null && req.getUnavailableUserId() != null) {
            UserAccount offlineUser = userRepository.findById(req.getUnavailableUserId()).orElse(null);
            if (offlineUser != null) {
                List<Task> userTasks = allTasks.stream()
                    .filter(t -> t.getAssignee() != null && t.getAssignee().getId().equals(offlineUser.getId()) && t.getStatus() != TaskStatus.DONE)
                    .collect(Collectors.toList());
                res.getCascadeSteps().add("⚠️ [Capacity Shock] " + offlineUser.getFullName() + 
                    " is unavailable. " + userTasks.size() + " active task(s) immediately orphaned.");
                overdueCount += userTasks.size();
                dragIncrease += userTasks.size() * 25.0;
                if (criticalDownstreamTask == null && !userTasks.isEmpty()) {
                    criticalDownstreamTask = userTasks.get(0);
                }
            }
        }

        double riskScore = Math.min(100.0, Math.round(((overdueCount * 28.0) + (dragIncrease * 0.3) + (delayHours * 0.35)) * 10.0) / 10.0);
        res.setProjectedFailureRisk(riskScore);
        res.setDragIncreasePoints(Math.round(dragIncrease * 10.0) / 10.0);
        res.setImpactedTasksCount(downstream.size());
        res.setOverdueTasksCount(overdueCount);

        if (riskScore >= 70.0) {
            res.setRiskLevel("CRITICAL_COLLAPSE");
        } else if (riskScore >= 35.0) {
            res.setRiskLevel("ELEVATED");
        } else {
            res.setRiskLevel("NOMINAL");
        }

        // Formulate Algorithmic Mitigation Countermeasure
        if (criticalDownstreamTask != null) {
            res.setSuggestedReassignmentTaskId(criticalDownstreamTask.getId());
            res.setSuggestedDeadlineExtensionHours(delayHours + 24);

            List<UserAccount> members = userRepository.findAll();
            Long excludeId = criticalDownstreamTask.getAssignee() != null ? criticalDownstreamTask.getAssignee().getId() : null;
            UserAccount bestAlternative = members.stream()
                .filter(u -> excludeId == null || !u.getId().equals(excludeId))
                .min(Comparator.comparingDouble(u -> allTasks.stream()
                    .filter(t -> t.getAssignee() != null && t.getAssignee().getId().equals(u.getId()) && t.getStatus() != TaskStatus.DONE)
                    .mapToDouble(t -> gravityCalculatorService.calculateGravity(t, allTasks).getTotalGravityScore())
                    .sum()))
                .orElse(null);

            if (bestAlternative != null) {
                res.setSuggestedReassignmentUserId(bestAlternative.getId());
                res.setSuggestedReassignmentUserName(bestAlternative.getFullName());
                res.setRecommendedMitigationPlan("Reassign Task #" + criticalDownstreamTask.getId() + 
                    " to " + bestAlternative.getFullName() + " and add a +" + (delayHours + 24) + "h safety buffer to restore sprint health.");
            } else {
                res.setRecommendedMitigationPlan("Extend downstream deadlines by +" + (delayHours + 24) + "h to absorb cascade shock.");
            }
        } else {
            res.setRecommendedMitigationPlan("Current schedule absorbs the " + delayHours + "h delay without breaching downstream delivery milestones.");
        }

        return res;
    }

    /**
     * 1-Click Mitigation Application:
     * Executes the algorithmic mitigation plan directly in the database.
     */
    @Transactional
    public List<TaskResponseDto> applySimulationMitigation(SprintSimulationRequest req) {
        SprintSimulationResponse simulation = simulateSprintScenario(req);

        if (simulation.getSuggestedReassignmentTaskId() != null && simulation.getSuggestedReassignmentUserId() != null) {
            Task task = taskRepository.findById(simulation.getSuggestedReassignmentTaskId()).orElse(null);
            UserAccount newAssignee = userRepository.findById(simulation.getSuggestedReassignmentUserId()).orElse(null);
            if (task != null && newAssignee != null) {
                task.setAssignee(newAssignee);
                if (task.getDeadline() != null) {
                    task.setDeadline(task.getDeadline().plusHours(simulation.getSuggestedDeadlineExtensionHours()));
                } else {
                    task.setDeadline(LocalDateTime.now().plusHours(simulation.getSuggestedDeadlineExtensionHours()));
                }
                task.setPinged(false);
                task.setPingCount(0);
                task.setLastActivityAt(LocalDateTime.now());
                taskRepository.save(task);
            }
        }

        return getAllTasks();
    }
}
