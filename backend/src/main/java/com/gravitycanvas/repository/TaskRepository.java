package com.gravitycanvas.repository;

import com.gravitycanvas.entity.Task;
import com.gravitycanvas.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * Counts active tasks for a specific user (status != DONE).
     * Used by GravityCalculatorService for calculating Workload Density Score.
     */
    long countByAssigneeIdAndStatusNot(Long assigneeId, TaskStatus status);
}
