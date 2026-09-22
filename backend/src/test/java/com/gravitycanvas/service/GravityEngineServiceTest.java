package com.gravitycanvas.service;

import com.gravitycanvas.entity.Task;
import com.gravitycanvas.entity.TaskEnvironment;
import com.gravitycanvas.entity.TaskStatus;
import com.gravitycanvas.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Comprehensive JUnit 5 Unit Tests for GravityEngineService.
 * Validates exact two-phase deadline proximity calculation & capping rules:
 * 1. Task due in 24 hours -> ~50.0 pts.
 * 2. Task due in 0 hours -> 100.0 pts.
 * 3. Task overdue by 5 hours -> 105.0 pts.
 * 4. Task overdue by 100 hours -> 130.0 pts (capped at +30.0 max penalty).
 * 5. Logarithmic Blast Radius scaling.
 * 6. Stagnation pauses during IN_REVIEW.
 * 7. Instant +25 points boost for BLOCKED.
 * 8. Environment Multipliers (PROD 1.5x vs STAGING 1.2x vs DEV 1.0x).
 * 9. Status Override Rule (DONE status forces ORBIT zone).
 */
class GravityEngineServiceTest {

    private TaskRepository taskRepository;
    private GravityEngineService gravityEngineService;

    @BeforeEach
    void setUp() {
        taskRepository = Mockito.mock(TaskRepository.class);
        gravityEngineService = new GravityEngineService(taskRepository);

        when(taskRepository.countByAssigneeIdAndStatusNot(anyLong(), eq(TaskStatus.DONE))).thenReturn(0L);
    }

    @Test
    @DisplayName("Deadline Test 1: Task due in 24 hours -> expected deadline score ~50.0 pts")
    void testDeadlineScore24Hours() {
        double score = gravityEngineService.calculateDeadlineScore(24);
        assertEquals(50.0, score, 0.1, "Score for task due in 24 hours should be 50.0 pts");
    }

    @Test
    @DisplayName("Deadline Test 2: Task due in 0 hours -> expected deadline score 100.0 pts")
    void testDeadlineScore0Hours() {
        double score = gravityEngineService.calculateDeadlineScore(0);
        assertEquals(100.0, score, 0.1, "Score for task due in 0 hours should be 100.0 pts");
    }

    @Test
    @DisplayName("Deadline Test 3: Task overdue by 5 hours -> expected deadline score 105.0 pts")
    void testDeadlineScoreOverdue5Hours() {
        double score = gravityEngineService.calculateDeadlineScore(-5);
        assertEquals(105.0, score, 0.1, "Score for task overdue by 5 hours should be 105.0 pts");
    }

    @Test
    @DisplayName("Deadline Test 4: Task overdue by 100 hours -> expected deadline score 130.0 pts (capped)")
    void testDeadlineScoreOverdue100HoursCapped() {
        double score = gravityEngineService.calculateDeadlineScore(-100);
        assertEquals(130.0, score, 0.1, "Score for task overdue by 100 hours should be capped at 130.0 pts");
    }

    @Test
    @DisplayName("Test 5: Verify Logarithmic Blast Radius Scaling")
    void testLogarithmicBlastRadiusScaling() {
        Task directTask1 = new Task();
        directTask1.setId(2L);
        directTask1.setDependentTasks(new HashSet<>());

        Task indirectTask1 = new Task();
        indirectTask1.setId(3L);
        indirectTask1.setDependentTasks(new HashSet<>());
        directTask1.getDependentTasks().add(indirectTask1);

        Task mainTask = new Task();
        mainTask.setId(1L);
        mainTask.setStatus(TaskStatus.IN_PROGRESS);
        mainTask.setEnvironment(TaskEnvironment.DEVELOPMENT);
        mainTask.setDeadline(LocalDateTime.now().plusHours(24));
        mainTask.setLastActivityAt(LocalDateTime.now());

        Set<Task> dependents = new HashSet<>();
        dependents.add(directTask1);
        mainTask.setDependentTasks(dependents);

        double score = gravityEngineService.calculateGravityScore(mainTask);
        assertTrue(score > 50.0, "Score should include blast radius points on top of base urgency");
    }

    @Test
    @DisplayName("Test 6: Verify Stagnation Pauses During IN_REVIEW")
    void testStagnationPausesDuringInReview() {
        LocalDateTime daysAgo = LocalDateTime.now().minusDays(10);

        Task activeTask = new Task();
        activeTask.setStatus(TaskStatus.IN_PROGRESS);
        activeTask.setEnvironment(TaskEnvironment.DEVELOPMENT);
        activeTask.setDeadline(LocalDateTime.now().plusHours(24));
        activeTask.setLastActivityAt(daysAgo);

        Task reviewTask = new Task();
        reviewTask.setStatus(TaskStatus.IN_REVIEW);
        reviewTask.setEnvironment(TaskEnvironment.DEVELOPMENT);
        reviewTask.setDeadline(LocalDateTime.now().plusHours(24));
        reviewTask.setLastActivityAt(daysAgo);

        double scoreActive = gravityEngineService.calculateGravityScore(activeTask);
        double scoreReview = gravityEngineService.calculateGravityScore(reviewTask);

        assertTrue(scoreActive > scoreReview, "IN_PROGRESS score (" + scoreActive + ") should accumulate stagnation points over IN_REVIEW (" + scoreReview + ")");
    }

    @Test
    @DisplayName("Test 7: Verify Instant +25 Points Boost When Status Changes to BLOCKED")
    void testInstantBlockerPenaltyBoost() {
        LocalDateTime futureDeadline = LocalDateTime.now().plusHours(24);

        Task normalTask = new Task();
        normalTask.setStatus(TaskStatus.IN_PROGRESS);
        normalTask.setEnvironment(TaskEnvironment.DEVELOPMENT);
        normalTask.setDeadline(futureDeadline);
        normalTask.setLastActivityAt(LocalDateTime.now());

        Task blockedTask = new Task();
        blockedTask.setStatus(TaskStatus.BLOCKED);
        blockedTask.setEnvironment(TaskEnvironment.DEVELOPMENT);
        blockedTask.setDeadline(futureDeadline);
        blockedTask.setLastActivityAt(LocalDateTime.now());

        double normalScore = gravityEngineService.calculateGravityScore(normalTask);
        double blockedScore = gravityEngineService.calculateGravityScore(blockedTask);

        assertEquals(25.0, Math.round((blockedScore - normalScore) * 10.0) / 10.0, "BLOCKED status should add exactly +25.0 points boost");
    }

    @Test
    @DisplayName("Test 8: Verify Environment Multipliers (PROD 1.5x vs STAGING 1.2x vs DEV 1.0x)")
    void testEnvironmentMultipliers() {
        LocalDateTime deadline = LocalDateTime.now().plusHours(24);

        Task devTask = new Task();
        devTask.setStatus(TaskStatus.IN_PROGRESS);
        devTask.setEnvironment(TaskEnvironment.DEVELOPMENT);
        devTask.setDeadline(deadline);
        devTask.setLastActivityAt(LocalDateTime.now());

        Task stagingTask = new Task();
        stagingTask.setStatus(TaskStatus.IN_PROGRESS);
        stagingTask.setEnvironment(TaskEnvironment.STAGING);
        stagingTask.setDeadline(deadline);
        stagingTask.setLastActivityAt(LocalDateTime.now());

        Task prodTask = new Task();
        prodTask.setStatus(TaskStatus.IN_PROGRESS);
        prodTask.setEnvironment(TaskEnvironment.PRODUCTION);
        prodTask.setDeadline(deadline);
        prodTask.setLastActivityAt(LocalDateTime.now());

        double devScore = gravityEngineService.calculateGravityScore(devTask);
        double stagingScore = gravityEngineService.calculateGravityScore(stagingTask);
        double prodScore = gravityEngineService.calculateGravityScore(prodTask);

        assertEquals(devScore * 1.2, stagingScore, 1.0, "STAGING score should be 1.2x DEV score");
        assertEquals(devScore * 1.5, prodScore, 1.0, "PROD score should be 1.5x DEV score");
    }

    @Test
    @DisplayName("Test 9: Verify Status Override Rule (DONE Status Forces ORBIT Zone)")
    void testDoneStatusForcesOrbitZone() {
        Task doneTask = new Task();
        doneTask.setStatus(TaskStatus.DONE);
        doneTask.setEnvironment(TaskEnvironment.PRODUCTION);
        doneTask.setDeadline(LocalDateTime.now().minusHours(100));

        String zone = gravityEngineService.determineCanvasZone(doneTask, 150.0);
        assertEquals("ORBIT", zone, "Tasks with DONE status MUST evaluate as ORBIT regardless of raw score");
    }
}
