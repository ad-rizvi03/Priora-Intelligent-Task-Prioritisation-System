package com.gravitycanvas.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Task entity holding attributes required for the Gravity Score formula calculation,
 * ping toggle alerts, custom factor application, and task lifecycle tracking.
 */
@Entity
@Table(name = "TASKS")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(name = "APPROVAL_STATUS", nullable = false, length = 30)
    private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

    @Column(name = "REJECTION_REASON", length = 1000)
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskEnvironment environment = TaskEnvironment.DEVELOPMENT;

    @Column(name = "CUSTOM_FACTOR_APPLIED", nullable = false)
    private boolean customFactorApplied = false;

    @Column(name = "PINGED", nullable = false)
    private boolean pinged = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ASSIGNEE_ID")
    private UserAccount assignee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CREATED_BY_ID")
    private UserAccount createdBy;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "LAST_ACTIVITY_AT", nullable = false)
    private LocalDateTime lastActivityAt = LocalDateTime.now();

    @Column(name = "DEADLINE", nullable = false)
    private LocalDateTime deadline;

    @Column(name = "PING_COUNT", nullable = false)
    private int pingCount = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "TASK_DEPENDENCIES",
        joinColumns = @JoinColumn(name = "PARENT_TASK_ID"),
        inverseJoinColumns = @JoinColumn(name = "DEPENDENT_TASK_ID")
    )
    private Set<Task> dependentTasks = new HashSet<>();

    @ManyToMany(mappedBy = "dependentTasks", fetch = FetchType.EAGER)
    private Set<Task> dependencies = new HashSet<>();

    public Task() {}

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

    public UserAccount getAssignee() { return assignee; }
    public void setAssignee(UserAccount assignee) { this.assignee = assignee; }

    public UserAccount getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserAccount createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public int getPingCount() { return pingCount; }
    public void setPingCount(int pingCount) { this.pingCount = pingCount; }

    public Set<Task> getDependentTasks() { return dependentTasks; }
    public void setDependentTasks(Set<Task> dependentTasks) { this.dependentTasks = dependentTasks; }

    public Set<Task> getDependencies() { return dependencies; }
    public void setDependencies(Set<Task> dependencies) { this.dependencies = dependencies; }
}
