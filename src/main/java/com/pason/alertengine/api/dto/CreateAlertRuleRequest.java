package com.pason.alertengine.api.dto;

import com.pason.alertengine.domain.model.AlertSeverity;
import com.pason.alertengine.domain.model.SensorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * Request body for creating a new alert rule.
 *
 * <p>The condition type determines which parameters are required in conditionConfig:</p>
 * <ul>
 *   <li>THRESHOLD_EXCEEDED: threshold (number), operator (GREATER_THAN/LESS_THAN)</li>
 *   <li>RATE_OF_CHANGE: maxRatePerSecond (number)</li>
 *   <li>SUSTAINED_THRESHOLD: threshold (number), operator, sustainedSeconds (integer)</li>
 *   <li>OUT_OF_RANGE: min (number), max (number)</li>
 * </ul>
 */
public record CreateAlertRuleRequest(
    @NotBlank(message = "Rule name is required")
    String name,

    @NotNull(message = "Sensor type is required")
    SensorType sensorType,

    @NotBlank(message = "Condition type is required")
    String conditionType,

    @NotNull(message = "Condition configuration is required")
    Map<String, Object> conditionConfig,

    @NotNull(message = "Severity is required")
    AlertSeverity severity
) {}
