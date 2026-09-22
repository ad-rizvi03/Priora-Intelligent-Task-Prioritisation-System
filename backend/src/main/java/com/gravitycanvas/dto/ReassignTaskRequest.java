package com.gravitycanvas.dto;

public class ReassignTaskRequest {
    private Long assigneeId;

    public ReassignTaskRequest() {}

    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
}
