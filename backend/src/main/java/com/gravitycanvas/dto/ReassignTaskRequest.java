package com.gravitycanvas.dto;

public class ReassignTaskRequest {
    private Long assigneeId;
    private boolean overrideCircuitBreaker;

    public ReassignTaskRequest() {}

    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }

    public boolean isOverrideCircuitBreaker() { return overrideCircuitBreaker; }
    public void setOverrideCircuitBreaker(boolean overrideCircuitBreaker) { this.overrideCircuitBreaker = overrideCircuitBreaker; }
}
