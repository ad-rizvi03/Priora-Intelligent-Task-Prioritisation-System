package com.gravitycanvas.dto;

import com.gravitycanvas.entity.ApprovalStatus;
import com.gravitycanvas.entity.TaskEnvironment;
import com.gravitycanvas.entity.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

public class TaskResponseDto {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private ApprovalStatus approvalStatus;
    private String rejectionReason;
    private TaskEnvironment environment;
    private boolean customFactorApplied;
    private boolean pinged;
    private UserDto assignee;
    private UserDto createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivityAt;
    private LocalDateTime deadline;
    private int pingCount;
    private List<Long> dependentTaskIds;
    private List<Long> dependencyIds;

    // --- Dynamic Gravity Engine Calculation Metrics ---
    private double totalGravityScore;
    
    // Formula Components Breakdown
    private double dependencyScore;
    private int directBlockedCount;
    private int indirectBlockedCount;
    
    private double deadlineProximityScore;
    private long hoursUntilDeadline;
    private boolean overdue;
    
    private double stagnationScore;
    private long hoursInactive;
    
    private double workloadDensityScore;
    private long assigneeActiveTasksCount;
    
    private String customFactorName;
    private double customFactorScore;

    private double environmentMultiplier;
    private String canvasZone; // "FOCUS", "DRIFTING", "ORBIT"

    public TaskResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public TaskEnvironment getEnvironment() { return environment; }
    public void setEnvironment(TaskEnvironment environment) { this.environment = environment; }

    public boolean isCustomFactorApplied() { return customFactorApplied; }
    public void setCustomFactorApplied(boolean customFactorApplied) { this.customFactorApplied = customFactorApplied; }

    public boolean isPinged() { return pinged; }
    public void setPinged(boolean pinged) { this.pinged = pinged; }

    public UserDto getAssignee() { return assignee; }
    public void setAssignee(UserDto assignee) { this.assignee = assignee; }

    public UserDto getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserDto createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public int getPingCount() { return pingCount; }
    public void setPingCount(int pingCount) { this.pingCount = pingCount; }

    public List<Long> getDependentTaskIds() { return dependentTaskIds; }
    public void setDependentTaskIds(List<Long> dependentTaskIds) { this.dependentTaskIds = dependentTaskIds; }

    public List<Long> getDependencyIds() { return dependencyIds; }
    public void setDependencyIds(List<Long> dependencyIds) { this.dependencyIds = dependencyIds; }

    public double getTotalGravityScore() { return totalGravityScore; }
    public void setTotalGravityScore(double totalGravityScore) { this.totalGravityScore = totalGravityScore; }

    public double getDependencyScore() { return dependencyScore; }
    public void setDependencyScore(double dependencyScore) { this.dependencyScore = dependencyScore; }

    public int getDirectBlockedCount() { return directBlockedCount; }
    public void setDirectBlockedCount(int directBlockedCount) { this.directBlockedCount = directBlockedCount; }

    public int getIndirectBlockedCount() { return indirectBlockedCount; }
    public void setIndirectBlockedCount(int indirectBlockedCount) { this.indirectBlockedCount = indirectBlockedCount; }

    public double getDeadlineProximityScore() { return deadlineProximityScore; }
    public void setDeadlineProximityScore(double deadlineProximityScore) { this.deadlineProximityScore = deadlineProximityScore; }

    public long getHoursUntilDeadline() { return hoursUntilDeadline; }
    public void setHoursUntilDeadline(long hoursUntilDeadline) { this.hoursUntilDeadline = hoursUntilDeadline; }

    public boolean isOverdue() { return overdue; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }

    public double getStagnationScore() { return stagnationScore; }
    public void setStagnationScore(double stagnationScore) { this.stagnationScore = stagnationScore; }

    public long getHoursInactive() { return hoursInactive; }
    public void setHoursInactive(long hoursInactive) { this.hoursInactive = hoursInactive; }

    public double getWorkloadDensityScore() { return workloadDensityScore; }
    public void setWorkloadDensityScore(double workloadDensityScore) { this.workloadDensityScore = workloadDensityScore; }

    public long getAssigneeActiveTasksCount() { return assigneeActiveTasksCount; }
    public void setAssigneeActiveTasksCount(long assigneeActiveTasksCount) { this.assigneeActiveTasksCount = assigneeActiveTasksCount; }

    public String getCustomFactorName() { return customFactorName; }
    public void setCustomFactorName(String customFactorName) { this.customFactorName = customFactorName; }

    public double getCustomFactorScore() { return customFactorScore; }
    public void setCustomFactorScore(double customFactorScore) { this.customFactorScore = customFactorScore; }

    public double getEnvironmentMultiplier() { return environmentMultiplier; }
    public void setEnvironmentMultiplier(double environmentMultiplier) { this.environmentMultiplier = environmentMultiplier; }

    public String getCanvasZone() { return canvasZone; }
    public void setCanvasZone(String canvasZone) { this.canvasZone = canvasZone; }
}
