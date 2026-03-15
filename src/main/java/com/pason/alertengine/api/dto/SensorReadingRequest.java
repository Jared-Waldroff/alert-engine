package com.pason.alertengine.api.dto;

import com.pason.alertengine.domain.model.SensorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for submitting a sensor reading to the engine.
 */
public record SensorReadingRequest(
    @NotBlank(message = "Sensor ID is required")
    String sensorId,

    @NotNull(message = "Sensor type is required")
    SensorType sensorType,

    @NotNull(message = "Value is required")
    Double value,

    @NotBlank(message = "Unit is required")
    String unit
) {}
