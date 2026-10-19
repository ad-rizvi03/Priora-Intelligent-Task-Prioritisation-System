package com.gravitycanvas.service;

import com.gravitycanvas.dto.TaskResponseDto;
import com.gravitycanvas.dto.UserDto;
import com.gravitycanvas.entity.GravitySettings;
import com.gravitycanvas.entity.Task;
import com.gravitycanvas.entity.TaskStatus;
import com.gravitycanvas.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adapter Service bridging GravityEngineService with TaskResponseDto for REST API responses.
 * Calculates total gravity score and populates all breakdown metric fields on TaskResponseDto.
 */
@Service
public class GravityCalculatorService {

    private final GravityEngineService gravityEngineService;
    private final TaskRepository taskRepository;
    private final SettingsService settingsService;

    public GravityCalculatorService(GravityEngineService gravityEngineService,
                                    TaskRepository taskRepository,
                                    SettingsService settingsService) {
        this.gravityEngineService = gravityEngineService;
        this.taskRepository = taskRepository;
        this.settingsService = settingsService;
    }

    public TaskResponseDto calculateGravity(Task task, List<Task> allTasks) {
        TaskResponseDto dto = new TaskResponseDto();

        // 1. Copy Basic Task Attributes
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setApprovalStatus(task.getApprovalStatus());
        dto.setRejectionReason(task.getRejectionReason());
        dto.setEnvironment(task.getEnvironment());
        dto.setCustomFactorApplied(task.isCustomFactorApplied());
        dto.setPinged(task.isPinged());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setLastActivityAt(task.getLastActivityAt());
        dto.setDeadline(task.getDeadline());
        dto.setPingCount(task.getPingCount());

        // Assignee Details
        if (task.getAssignee() != null) {
            dto.setAssignee(new UserDto(
                task.getAssignee().getId(),
                task.getAssignee().getUsername(),
                task.getAssignee().getFullName(),
                task.getAssignee().getRole(),
                task.getAssignee().getEmployeeNo(),
                task.getAssignee().getDepartment()
            ));
        }

        // Creator Details
        if (task.getCreatedBy() != null) {
            dto.setCreatedBy(new UserDto(
                task.getCreatedBy().getId(),
                task.getCreatedBy().getUsername(),
                task.getCreatedBy().getFullName(),
                task.getCreatedBy().getRole(),
                task.getCreatedBy().getEmployeeNo(),
                task.getCreatedBy().getDepartment()
            ));
        }

        // Direct Dependency IDs
        dto.setDependentTaskIds(
            task.getDependentTasks() != null ? task.getDependentTasks().stream().map(Task::getId).collect(Collectors.toList()) : List.of()
        );
        dto.setDependencyIds(
            task.getDependencies() != null ? task.getDependencies().stream().map(Task::getId).collect(Collectors.toList()) : List.of()
        );

        LocalDateTime now = LocalDateTime.now();

        // Calculate Gravity Score Breakdown Metrics
        int directCount = task.getDependentTasks() != null ? task.getDependentTasks().size() : 0;
        int indirectCount = calculateIndirectDownstreamCount(task);
        double blastRadius = (directCount * 15.0) + (5.0 * (Math.log(1 + indirectCount) / Math.log(2.0)));

        long hoursRemaining = (task.getDeadline() != null) ? Duration.between(now, task.getDeadline()).toHours() : 0;
        boolean isOverdue = (task.getDeadline() != null) && now.isAfter(task.getDeadline());
        
        // Two-Phase Deadline Score Calculation (0.0 to 130.0 pts max)
        double deadlineUrgency = gravityEngineService.calculateDeadlineScore(hoursRemaining);

        double stagnation = 0.0;
        long hoursInactive = 0;
        if (task.getLastActivityAt() != null) {
            hoursInactive = Math.max(0, Duration.between(task.getLastActivityAt(), now).toHours());
            if (task.getStatus() != TaskStatus.IN_REVIEW && task.getStatus() != TaskStatus.DONE) {
                double daysInactive = Math.max(0.0, Duration.between(task.getLastActivityAt(), now).toMinutes() / (60.0 * 24.0));
                stagnation = Math.min(15.0, (daysInactive / 7.0) * 5.0);
            }
        }

        long activeAssigneeTasks = 0;
        if (task.getAssignee() != null) {
            activeAssigneeTasks = taskRepository.countByAssigneeIdAndStatusNot(
                task.getAssignee().getId(), TaskStatus.DONE
            );
        }
        double workloadDensityScore = activeAssigneeTasks * 2.0;

        // Custom Weight Factor (if enabled & applied)
        GravitySettings settings = settingsService != null ? settingsService.getSettings() : null;
        double customFactorScore = 0.0;
        if (settings != null && settings.isEnableCustomFactor()) {
            dto.setCustomFactorName(settings.getCustomFactorName());
            if (task.isCustomFactorApplied() && settings.getCustomFactorPoints() != null) {
                customFactorScore = settings.getCustomFactorPoints();
            }
        }

        // Delegate Total Score & Canvas Zone calculation to GravityEngineService
        double totalGravityScore = gravityEngineService.calculateGravityScore(task);
        String zone = gravityEngineService.determineCanvasZone(task, totalGravityScore);

        // Populate DTO Breakdown Fields for UI Display
        dto.setDirectBlockedCount(directCount);
        dto.setIndirectBlockedCount(indirectCount);
        dto.setDependencyScore(Math.round(blastRadius * 10.0) / 10.0);

        dto.setDeadlineProximityScore(Math.round(deadlineUrgency * 10.0) / 10.0);
        dto.setHoursUntilDeadline(hoursRemaining);
        dto.setOverdue(isOverdue);

        dto.setStagnationScore(Math.round(stagnation * 10.0) / 10.0);
        dto.setHoursInactive(hoursInactive);

        dto.setAssigneeActiveTasksCount(activeAssigneeTasks);
        dto.setWorkloadDensityScore(Math.round(workloadDensityScore * 10.0) / 10.0);

        dto.setCustomFactorScore(customFactorScore);

        dto.setEnvironmentMultiplier(task.getEnvironment() != null ? task.getEnvironment().getMultiplier() : 1.0);
        dto.setTotalGravityScore(totalGravityScore);
        dto.setCanvasZone(zone);

        // --- Feature 2: Reverse Blast Radius & Domino of the Day Metrics ---
        Set<Task> allDownstream = getAllDownstreamTasks(task);
        if (task.getStatus() != TaskStatus.DONE && !allDownstream.isEmpty()) {
            int unblockedTasks = allDownstream.size();
            Set<Long> uniqueAssigneeIds = new HashSet<>();
            double releasedGravity = 0.0;
            for (Task downstream : allDownstream) {
                if (downstream.getAssignee() != null) {
                    uniqueAssigneeIds.add(downstream.getAssignee().getId());
                }
                releasedGravity += gravityEngineService.calculateGravityScore(downstream);
            }
            int unblockedEngineers = uniqueAssigneeIds.size();

            // Domino Score = (direct * 25) + (indirect * 15) + (engineers * 20) + (gravity * 0.25)
            double dominoScore = (directCount * 25.0) + (indirectCount * 15.0) 
                + (unblockedEngineers * 20.0) + (releasedGravity * 0.25);

            dto.setDominoLeverageScore(Math.round(dominoScore * 10.0) / 10.0);
            dto.setUnblockedTasksCount(unblockedTasks);
            dto.setUnblockedEngineersCount(unblockedEngineers);
            dto.setReleasedGravityPoints(Math.round(releasedGravity * 10.0) / 10.0);
        } else {
            dto.setDominoLeverageScore(0.0);
            dto.setUnblockedTasksCount(0);
            dto.setUnblockedEngineersCount(0);
            dto.setReleasedGravityPoints(0.0);
        }

        // --- Feature 3: Entropy Decay Detector & Zombie Defibrillator Metrics ---
        if (task.getStatus() == TaskStatus.DONE) {
            dto.setEntropyScore(0.0);
            dto.setEntropyLevel("FRESH");
            dto.setDefibrillatorAvailable(false);
        } else {
            double entropy = 0.0;
            // 1. Inactivity Factor: 10 points per 24 hours of inactivity (capped at 40 pts)
            double inactivityPoints = Math.min(40.0, (hoursInactive / 24.0) * 10.0);
            entropy += inactivityPoints;

            // 2. Ping Ignored Factor: 15 points per unanswered ping (capped at 30 pts)
            double pingPoints = Math.min(30.0, task.getPingCount() * 15.0);
            entropy += pingPoints;

            // 3. Stagnation in critical statuses: if in IN_PROGRESS or BLOCKED for >= 24 hours
            if ((task.getStatus() == TaskStatus.IN_PROGRESS || task.getStatus() == TaskStatus.BLOCKED) && hoursInactive >= 24) {
                entropy += 20.0;
            }

            // 4. Overdue Penalty: 20 points if past deadline
            if (isOverdue) {
                entropy += 20.0;
            }

            // 5. Blocker Friction: if blocking others, entropy carries higher systemic risk
            if (directCount > 0) {
                entropy += Math.min(15.0, directCount * 7.5);
            }

            entropy = Math.min(100.0, Math.max(0.0, Math.round(entropy * 10.0) / 10.0));
            dto.setEntropyScore(entropy);

            if (entropy >= 70.0) {
                dto.setEntropyLevel("ZOMBIE_BLOCKER");
                dto.setDefibrillatorAvailable(true);
            } else if (entropy >= 35.0) {
                dto.setEntropyLevel("STAGNATING");
                dto.setDefibrillatorAvailable(true);
            } else {
                dto.setEntropyLevel("FRESH");
                dto.setDefibrillatorAvailable(false);
            }
        }

        return dto;
    }

    private int calculateIndirectDownstreamCount(Task startTask) {
        int directCount = (startTask.getDependentTasks() != null) ? startTask.getDependentTasks().size() : 0;
        Set<Task> allDownstream = getAllDownstreamTasks(startTask);
        return Math.max(0, allDownstream.size() - directCount);
    }

    public Set<Task> getAllDownstreamTasks(Task startTask) {
        if (startTask.getDependentTasks() == null || startTask.getDependentTasks().isEmpty()) {
            return java.util.Collections.emptySet();
        }

        Set<Task> visited = new HashSet<>();
        Queue<Task> queue = new LinkedList<>();

        for (Task directDep : startTask.getDependentTasks()) {
            if (directDep != null) {
                visited.add(directDep);
                queue.add(directDep);
            }
        }

        while (!queue.isEmpty()) {
            Task current = queue.poll();
            if (current.getDependentTasks() != null) {
                for (Task nextDownstream : current.getDependentTasks()) {
                    if (nextDownstream != null && !visited.contains(nextDownstream)) {
                        visited.add(nextDownstream);
                        queue.add(nextDownstream);
                    }
                }
            }
        }

        return visited;
    }
}
