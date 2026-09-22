package com.gravitycanvas.dto;

import com.gravitycanvas.entity.TaskEnvironment;
import java.util.List;

public class CreateTaskRequest {
    private String title;
    private String description;
    private TaskEnvironment environment;
    private Long assigneeId;
    private java.time.LocalDateTime deadline;
    private List<Long> parentTaskIds;
    private boolean customFactorApplied;

    public CreateTaskRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskEnvironment getEnvironment() { return environment; }
    public void setEnvironment(TaskEnvironment environment) { this.environment = environment; }

    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }

    public java.time.LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(java.time.LocalDateTime deadline) { this.deadline = deadline; }

    public List<Long> getParentTaskIds() { return parentTaskIds; }
    public void setParentTaskIds(List<Long> parentTaskIds) { this.parentTaskIds = parentTaskIds; }

    public boolean isCustomFactorApplied() { return customFactorApplied; }
    public void setCustomFactorApplied(boolean customFactorApplied) { this.customFactorApplied = customFactorApplied; }
}
