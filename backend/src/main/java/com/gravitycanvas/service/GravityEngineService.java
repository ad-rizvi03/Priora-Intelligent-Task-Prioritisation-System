package com.gravitycanvas.service;

import com.gravitycanvas.entity.Task;
import com.gravitycanvas.entity.TaskStatus;
import com.gravitycanvas.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Core Architectural Mechanics & Mathematical Specification Service.
 * Implements two-phase deadline proximity calculation:
 * - Phase 1: Approaching Deadline (Smooth Sigmoid S-Curve up to 100.0 pts)
 * - Phase 2: Overdue Escalation (Base 100.0 pts + 1.0 pt per hour overdue, capped at +30.0 pts max)
 * 
 * Total Gravity Score = [ Base Score ] * Workload Multiplier * Environment Multiplier
 * Where Base Score = Blast Radius + Deadline Urgency + Stagnation + Blocker Penalty
 */
@Service
public class GravityEngineService {

    private final TaskRepository taskRepository;

    public GravityEngineService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Calculates the Deadline Proximity Score using two-phase curve & escalation capping.
     * 
     * @param hoursRemaining Hours until task deadline (negative if overdue)
     * @return Calculated Deadline Proximity Score (range 0.0 to 130.0 pts max)
     */
    public double calculateDeadlineScore(long hoursRemaining) {
        // Phase 1: Approaching Deadline (Smooth Sigmoid S-Curve up to 100.0 pts)
        if (hoursRemaining > 0) {
            return 100.0 / (1.0 + Math.exp(0.1 * (hoursRemaining - 24.0)));
        }

        // Phase 2: Overdue Escalation (Base 100.0 pts + 1.0 pt per hour overdue, capped at +30.0 pts max)
        // Total Deadline Proximity Score ranges from 100.0 to 130.0 pts max.
        long hoursOverdue = Math.abs(hoursRemaining);
        double overdueEscalation = Math.min(30.0, hoursOverdue * 1.0);

        return 100.0 + overdueEscalation;
    }

    /**
     * Calculates the Total Gravity Score for a given task according to exact mathematical specs.
     * 
     * @param task Target task entity
     * @return Calculated Total Gravity Score
     */
    public double calculateGravityScore(Task task) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Blast Radius (Logarithmic Downstream Impact)
        int directCount = task.getDependentTasks() != null ? task.getDependentTasks().size() : 0;
        int indirectCount = calculateIndirectDownstreamCount(task);
        double blastRadius = (directCount * 15.0) + (5.0 * (Math.log(1 + indirectCount) / Math.log(2.0)));

        // 2. Deadline Urgency (Two-Phase Sigmoid & Escalation Capped at 130.0 pts)
        long hoursRemaining = 0;
        if (task.getDeadline() != null) {
            hoursRemaining = Duration.between(now, task.getDeadline()).toHours();
        }
        double deadlineUrgency = calculateDeadlineScore(hoursRemaining);

        // 3. Stagnation / Inactivity (Day-Based Aging)
        double stagnation = 0.0;
        if (task.getStatus() != TaskStatus.IN_REVIEW && task.getStatus() != TaskStatus.DONE) {
            LocalDateTime lastActivity = task.getLastActivityAt() != null ? task.getLastActivityAt() : task.getCreatedAt();
            if (lastActivity != null) {
                double daysInactive = Math.max(0.0, Duration.between(lastActivity, now).toMinutes() / (60.0 * 24.0));
                stagnation = Math.min(15.0, (daysInactive / 7.0) * 5.0);
            }
        }

        // 4. Instant Blocker Penalty
        double blockerPenalty = (task.getStatus() == TaskStatus.BLOCKED) ? 25.0 : 0.0;

        // SUM BASE SCORE
        double baseScore = blastRadius + deadlineUrgency + stagnation + blockerPenalty;

        // 5. Workload Multiplier (Assignee Bottleneck)
        long activeAssigneeTasks = 0;
        if (task.getAssignee() != null && taskRepository != null) {
            activeAssigneeTasks = taskRepository.countByAssigneeIdAndStatusNot(
                task.getAssignee().getId(), TaskStatus.DONE
            );
        }
        double workloadMultiplier = 1.0 + (0.05 * Math.max(0, activeAssigneeTasks - 3));

        // 6. Environment Multiplier
        double environmentMultiplier = (task.getEnvironment() != null) 
            ? task.getEnvironment().getMultiplier() 
            : 1.0;

        // FINAL TOTAL GRAVITY SCORE
        double totalGravityScore = baseScore * workloadMultiplier * environmentMultiplier;
        return Math.round(totalGravityScore * 10.0) / 10.0;
    }

    /**
     * Determines Canvas Zone ("FOCUS", "DRIFTING", "ORBIT") based on Gravity Score and Status Override Rule.
     */
    public String determineCanvasZone(Task task, double gravityScore) {
        if (task.getStatus() == TaskStatus.DONE) {
            return "ORBIT";
        }
        if (gravityScore >= 80.0) {
            return "FOCUS";
        }
        if (gravityScore >= 40.0) {
            return "DRIFTING";
        }
        return "ORBIT";
    }

    private int calculateIndirectDownstreamCount(Task startTask) {
        if (startTask.getDependentTasks() == null || startTask.getDependentTasks().isEmpty()) {
            return 0;
        }

        Set<Task> visited = new HashSet<>();
        Queue<Task> queue = new LinkedList<>();

        for (Task directDep : startTask.getDependentTasks()) {
            if (directDep != null) {
                visited.add(directDep);
                queue.add(directDep);
            }
        }

        int directCount = visited.size();

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

        return Math.max(0, visited.size() - directCount);
    }
}
