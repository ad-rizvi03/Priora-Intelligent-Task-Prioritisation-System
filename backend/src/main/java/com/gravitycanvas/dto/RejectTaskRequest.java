package com.gravitycanvas.dto;

public class RejectTaskRequest {
    private String rejectionReason;

    public RejectTaskRequest() {}

    public RejectTaskRequest(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
