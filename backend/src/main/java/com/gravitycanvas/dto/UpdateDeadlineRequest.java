package com.gravitycanvas.dto;

import java.time.LocalDateTime;

public class UpdateDeadlineRequest {
    private LocalDateTime deadline;

    public UpdateDeadlineRequest() {}

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
}
