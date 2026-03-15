package com.pason.alertengine.simulator;

import com.pason.alertengine.domain.model.SensorType;

/**
 * Defines the behavior of a simulated sensor including baseline,
 * noise range, and anomaly characteristics.
 *
 * @param sensorId          unique identifier for this sensor instance
 * @param sensorType        the type of measurement (PRESSURE, TEMPERATURE, etc.)
 * @param baseline          normal operating value
 * @param noiseRange        max random deviation from baseline during normal operation
 * @param unit              measurement unit (PSI, °F, GPM, %, RPM)
 * @param anomalyValue      value during an anomaly event
 * @param anomalyProbability chance of anomaly per reading (0.0 to 1.0)
 */
public record SensorProfile(
    String sensorId,
    SensorType sensorType,
    double baseline,
    double noiseRange,
    String unit,
    double anomalyValue,
    double anomalyProbability
) {}
