package com.pason.alertengine.api.dto;

import com.pason.alertengine.domain.model.AlertSeverity;
import com.pason.alertengine.domain.model.SensorType;
import java.time.Instant;

/**
 * Response DTO for alert data returned by the API.
 */
public record AlertResponse(
    String alertId,
    String ruleName,
    AlertSeverity severity,
    String sensorId,
    SensorType sensorType,
    double triggerValue,
    double threshold,
    String conditionDescription,
    String message,
    Instant timestamp
) {}
