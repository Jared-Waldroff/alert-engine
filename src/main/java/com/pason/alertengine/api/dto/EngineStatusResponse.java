package com.pason.alertengine.api.dto;

/**
 * Response DTO for the engine's current status and metrics.
 *
 * @param readingsProcessed total sensor readings evaluated since engine start
 * @param alertsTriggered   total alerts triggered since engine start
 * @param activeRules       number of currently active alert rules
 * @param uptimeSeconds     engine uptime in seconds
 * @param simulatorRunning  whether the sensor simulator is active
 */
public record EngineStatusResponse(
    long readingsProcessed,
    long alertsTriggered,
    int activeRules,
    long uptimeSeconds,
    boolean simulatorRunning
) {}
