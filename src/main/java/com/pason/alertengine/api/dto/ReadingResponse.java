package com.pason.alertengine.api.dto;

import com.pason.alertengine.domain.model.SensorType;
import java.time.Instant;

/**
 * Response DTO for sensor reading data.
 */
public record ReadingResponse(
    String sensorId,
    SensorType sensorType,
    double value,
    String unit,
    Instant timestamp
) {}
