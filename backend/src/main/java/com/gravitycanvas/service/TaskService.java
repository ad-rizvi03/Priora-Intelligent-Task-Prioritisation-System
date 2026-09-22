package com.gravitycanvas.service;

import com.gravitycanvas.dto.*;
import com.gravitycanvas.entity.ApprovalStatus;
import com.gravitycanvas.entity.Task;
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
        return allTasks.stream()
            .filter(task -> task.getApprovalStatus() == ApprovalStatus.APPROVED)
            .map(task -> gravityCalculatorService.calculateGravity(task, allTasks))
            .sorted(Comparator.comparingDouble(TaskResponseDto::getTotalGravityScore).reversed())
            .collect(Collectors.toList());
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
            task.setAssignee(assignee);
        } else {
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
}
