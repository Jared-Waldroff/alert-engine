package com.pason.alertengine.api.dto;

import com.pason.alertengine.domain.model.AlertSeverity;
import com.pason.alertengine.domain.model.SensorType;
import java.time.Instant;

/**
 * Response DTO for alert rule data returned by the API.
 */
public record AlertRuleResponse(
    Long id,
    String name,
    SensorType sensorType,
    String conditionType,
    String conditionDescription,
    AlertSeverity severity,
    boolean enabled,
    Instant createdAt
) {}
