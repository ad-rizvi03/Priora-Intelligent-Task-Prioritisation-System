package com.gravitycanvas.dto;

/**
 * Request payload for Feature 4: What-If Sprint Time Machine & Cascade Simulator.
 */
public class SprintSimulationRequest {
    private Long delayedTaskId;
    private int delayHours = 24;
    private Long unavailableUserId;

    public SprintSimulationRequest() {}

    public SprintSimulationRequest(Long delayedTaskId, int delayHours, Long unavailableUserId) {
        this.delayedTaskId = delayedTaskId;
        this.delayHours = delayHours;
        this.unavailableUserId = unavailableUserId;
    }

    public Long getDelayedTaskId() {
        return delayedTaskId;
    }

    public void setDelayedTaskId(Long delayedTaskId) {
        this.delayedTaskId = delayedTaskId;
    }

    public int getDelayHours() {
        return delayHours;
    }

    public void setDelayHours(int delayHours) {
        this.delayHours = delayHours;
    }

    public Long getUnavailableUserId() {
        return unavailableUserId;
    }

    public void setUnavailableUserId(Long unavailableUserId) {
        this.unavailableUserId = unavailableUserId;
    }
}
