package com.gravitycanvas.entity;

/**
 * Clean Domain Enum defining deployment environments for tasks.
 * PRODUCTION = 1.5x
 * STAGING = 1.2x
 * DEVELOPMENT = 1.0x
 */
public enum EnvironmentType {
    PRODUCTION(1.5),
    STAGING(1.2),
    DEVELOPMENT(1.0);

    private final double multiplier;

    EnvironmentType(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
