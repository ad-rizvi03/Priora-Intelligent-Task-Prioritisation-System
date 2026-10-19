package com.gravitycanvas.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Response payload for Feature 4: What-If Sprint Time Machine & Cascade Simulator.
 */
public class SprintSimulationResponse {
    private double projectedFailureRisk;
    private String riskLevel; // "NOMINAL", "ELEVATED", "CRITICAL_COLLAPSE"
    private double dragIncreasePoints;
    private int impactedTasksCount;
    private int overdueTasksCount;
    private List<String> cascadeSteps = new ArrayList<>();
    private String recommendedMitigationPlan;
    private Long suggestedReassignmentTaskId;
    private Long suggestedReassignmentUserId;
    private String suggestedReassignmentUserName;
    private int suggestedDeadlineExtensionHours;

    public SprintSimulationResponse() {}

    public double getProjectedFailureRisk() {
        return projectedFailureRisk;
    }

    public void setProjectedFailureRisk(double projectedFailureRisk) {
        this.projectedFailureRisk = projectedFailureRisk;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public double getDragIncreasePoints() {
        return dragIncreasePoints;
    }

    public void setDragIncreasePoints(double dragIncreasePoints) {
        this.dragIncreasePoints = dragIncreasePoints;
    }

    public int getImpactedTasksCount() {
        return impactedTasksCount;
    }

    public void setImpactedTasksCount(int impactedTasksCount) {
        this.impactedTasksCount = impactedTasksCount;
    }

    public int getOverdueTasksCount() {
        return overdueTasksCount;
    }

    public void setOverdueTasksCount(int overdueTasksCount) {
        this.overdueTasksCount = overdueTasksCount;
    }

    public List<String> getCascadeSteps() {
        return cascadeSteps;
    }

    public void setCascadeSteps(List<String> cascadeSteps) {
        this.cascadeSteps = cascadeSteps;
    }

    public String getRecommendedMitigationPlan() {
        return recommendedMitigationPlan;
    }

    public void setRecommendedMitigationPlan(String recommendedMitigationPlan) {
        this.recommendedMitigationPlan = recommendedMitigationPlan;
    }

    public Long getSuggestedReassignmentTaskId() {
        return suggestedReassignmentTaskId;
    }

    public void setSuggestedReassignmentTaskId(Long suggestedReassignmentTaskId) {
        this.suggestedReassignmentTaskId = suggestedReassignmentTaskId;
    }

    public Long getSuggestedReassignmentUserId() {
        return suggestedReassignmentUserId;
    }

    public void setSuggestedReassignmentUserId(Long suggestedReassignmentUserId) {
        this.suggestedReassignmentUserId = suggestedReassignmentUserId;
    }

    public String getSuggestedReassignmentUserName() {
        return suggestedReassignmentUserName;
    }

    public void setSuggestedReassignmentUserName(String suggestedReassignmentUserName) {
        this.suggestedReassignmentUserName = suggestedReassignmentUserName;
    }

    public int getSuggestedDeadlineExtensionHours() {
        return suggestedDeadlineExtensionHours;
    }

    public void setSuggestedDeadlineExtensionHours(int suggestedDeadlineExtensionHours) {
        this.suggestedDeadlineExtensionHours = suggestedDeadlineExtensionHours;
    }
}
