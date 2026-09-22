package com.gravitycanvas.controller;

import com.gravitycanvas.dto.*;
import com.gravitycanvas.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing Task operations and Developer Ticket Authorization Workflows.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * GET /api/tasks
     * Allowed Roles: ROLE_MANAGER, ROLE_TEAM_MEMBER
     * Retrieves all APPROVED tasks evaluated with dynamic Gravity Scores.
     */
    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    /**
     * GET /api/tasks/pending
     * Allowed Roles: ROLE_MANAGER ONLY
     * Retrieves developer-submitted tickets awaiting manager approval.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<TaskResponseDto>> getPendingTasks() {
        return ResponseEntity.ok(taskService.getPendingTasks());
    }

    /**
     * POST /api/tasks
     * Allowed Roles: ROLE_MANAGER, ROLE_TEAM_MEMBER
     * Raises a new task/ticket. If created by developer, evaluates approval policy.
     */
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@RequestBody CreateTaskRequest request) {
        TaskResponseDto created = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/tasks/{id}/approve
     * Allowed Roles: ROLE_MANAGER ONLY
     * Manager authorizes developer ticket.
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<TaskResponseDto> approveTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.approveTask(id));
    }

    /**
     * PUT /api/tasks/{id}/reject
     * Allowed Roles: ROLE_MANAGER ONLY
     * Manager rejects developer ticket with reason.
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<TaskResponseDto> rejectTask(@PathVariable Long id, @RequestBody(required = false) RejectTaskRequest request) {
        return ResponseEntity.ok(taskService.rejectTask(id, request));
    }

    /**
     * DELETE /api/tasks/{id}
     * Allowed Roles: ROLE_MANAGER ONLY
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/tasks/{id}/deadline
     * Allowed Roles: ROLE_MANAGER ONLY
     */
    @PutMapping("/{id}/deadline")
    public ResponseEntity<TaskResponseDto> updateDeadline(@PathVariable Long id, @RequestBody UpdateDeadlineRequest request) {
        TaskResponseDto updated = taskService.updateDeadline(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * PUT /api/tasks/{id}/reassign
     * Allowed Roles: ROLE_MANAGER ONLY
     */
    @PutMapping("/{id}/reassign")
    public ResponseEntity<TaskResponseDto> reassignTask(@PathVariable Long id, @RequestBody ReassignTaskRequest request) {
        TaskResponseDto updated = taskService.reassignTask(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * PUT /api/tasks/{id}/status
     * Allowed Roles: ROLE_MANAGER, ROLE_TEAM_MEMBER
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<TaskResponseDto> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        TaskResponseDto updated = taskService.updateStatus(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * POST /api/tasks/{id}/ping
     * Allowed Roles: ROLE_MANAGER, ROLE_TEAM_MEMBER
     */
    @PostMapping("/{id}/ping")
    public ResponseEntity<TaskResponseDto> pingBlocker(@PathVariable Long id) {
        TaskResponseDto updated = taskService.pingBlocker(id);
        return ResponseEntity.ok(updated);
    }
}
