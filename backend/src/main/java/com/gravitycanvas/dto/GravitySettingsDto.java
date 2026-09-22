package com.gravitycanvas.dto;

public class GravitySettingsDto {
    private boolean enableDependencyScore;
    private boolean enableDeadlineProximityScore;
    private boolean enableStagnationScore;
    private boolean enableWorkloadDensityScore;
    private boolean requireApprovalForDevTasks;

    // Custom Manager Factor Fields
    private boolean enableCustomFactor;
    private String customFactorName;
    private Double customFactorPoints;

    public GravitySettingsDto() {}

    public GravitySettingsDto(boolean enableDependencyScore, boolean enableDeadlineProximityScore,
                              boolean enableStagnationScore, boolean enableWorkloadDensityScore,
                              boolean requireApprovalForDevTasks) {
        this.enableDependencyScore = enableDependencyScore;
        this.enableDeadlineProximityScore = enableDeadlineProximityScore;
        this.enableStagnationScore = enableStagnationScore;
        this.enableWorkloadDensityScore = enableWorkloadDensityScore;
        this.requireApprovalForDevTasks = requireApprovalForDevTasks;
    }

    public GravitySettingsDto(boolean enableDependencyScore, boolean enableDeadlineProximityScore,
                              boolean enableStagnationScore, boolean enableWorkloadDensityScore,
                              boolean requireApprovalForDevTasks, boolean enableCustomFactor,
                              String customFactorName, Double customFactorPoints) {
        this.enableDependencyScore = enableDependencyScore;
        this.enableDeadlineProximityScore = enableDeadlineProximityScore;
        this.enableStagnationScore = enableStagnationScore;
        this.enableWorkloadDensityScore = enableWorkloadDensityScore;
        this.requireApprovalForDevTasks = requireApprovalForDevTasks;
        this.enableCustomFactor = enableCustomFactor;
        this.customFactorName = customFactorName;
        this.customFactorPoints = customFactorPoints;
    }

    public boolean isEnableDependencyScore() { return enableDependencyScore; }
    public void setEnableDependencyScore(boolean enableDependencyScore) { this.enableDependencyScore = enableDependencyScore; }

    public boolean isEnableDeadlineProximityScore() { return enableDeadlineProximityScore; }
    public void setEnableDeadlineProximityScore(boolean enableDeadlineProximityScore) { this.enableDeadlineProximityScore = enableDeadlineProximityScore; }

    public boolean isEnableStagnationScore() { return enableStagnationScore; }
    public void setEnableStagnationScore(boolean enableStagnationScore) { this.enableStagnationScore = enableStagnationScore; }

    public boolean isEnableWorkloadDensityScore() { return enableWorkloadDensityScore; }
    public void setEnableWorkloadDensityScore(boolean enableWorkloadDensityScore) { this.enableWorkloadDensityScore = enableWorkloadDensityScore; }

    public boolean isRequireApprovalForDevTasks() { return requireApprovalForDevTasks; }
    public void setRequireApprovalForDevTasks(boolean requireApprovalForDevTasks) { this.requireApprovalForDevTasks = requireApprovalForDevTasks; }

    public boolean isEnableCustomFactor() { return enableCustomFactor; }
    public void setEnableCustomFactor(boolean enableCustomFactor) { this.enableCustomFactor = enableCustomFactor; }

    public String getCustomFactorName() { return customFactorName; }
    public void setCustomFactorName(String customFactorName) { this.customFactorName = customFactorName; }

    public Double getCustomFactorPoints() { return customFactorPoints; }
    public void setCustomFactorPoints(Double customFactorPoints) { this.customFactorPoints = customFactorPoints; }
}
