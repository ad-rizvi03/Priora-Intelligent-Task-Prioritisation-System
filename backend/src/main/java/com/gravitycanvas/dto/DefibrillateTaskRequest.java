package com.gravitycanvas.dto;

/**
 * Request payload for Feature 3: The 1-Click Ticket Defibrillator.
 * Supported actions: "SPLIT", "SHED_GRAVITY", "SMART_PASS", "DISSOLVE".
 */
public class DefibrillateTaskRequest {
    private String action;
    private Long targetAssigneeId;

    public DefibrillateTaskRequest() {}

    public DefibrillateTaskRequest(String action, Long targetAssigneeId) {
        this.action = action;
        this.targetAssigneeId = targetAssigneeId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getTargetAssigneeId() {
        return targetAssigneeId;
    }

    public void setTargetAssigneeId(Long targetAssigneeId) {
        this.targetAssigneeId = targetAssigneeId;
    }
}
