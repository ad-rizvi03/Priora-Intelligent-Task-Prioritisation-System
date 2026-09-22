package com.gravitycanvas.dto;

import com.gravitycanvas.entity.TaskStatus;

public class UpdateStatusRequest {
    private TaskStatus status;

    public UpdateStatusRequest() {}

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
}
