package com.pason.alertengine.domain.model;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.Objects;

/**
 * An immutable reading from a drilling rig sensor.
 *
 * <p>Readings are the fundamental unit of data in the alert engine.
 * Each reading captures a single measurement from a specific sensor
 * at a specific point in time. Readings are immutable once created,
 * ensuring thread-safe processing across concurrent sensor streams.</p>
 *
 * <p>Use the static factory method {@link #of(String, SensorType, double, String)}
 * for creating readings with the current timestamp, or the full constructor
 * for historical data.</p>
 */
public final class SensorReading {

  private final String sensorId;
  private final SensorType sensorType;
  private final double value;
  private final String unit;
  private final Instant timestamp;

  /**
   * Creates a new sensor reading.
   *
   * @param sensorId   unique identifier for the sensor that produced this reading
   * @param sensorType the type of measurement (PRESSURE, TEMPERATURE, etc.)
   * @param value      the measured value in the sensor's native unit
   * @param unit       the measurement unit (e.g., "PSI", "°F", "GPM")
   * @param timestamp  when the reading was taken
   * @throws IllegalArgumentException if sensorId or unit is null or blank
   */
  public SensorReading(String sensorId, SensorType sensorType, double value,
      String unit, Instant timestamp) {
    if (sensorId == null || sensorId.isBlank()) {
      throw new IllegalArgumentException("sensorId must not be null or blank");
    }
    if (sensorType == null) {
      throw new IllegalArgumentException("sensorType must not be null");
    }
    if (unit == null || unit.isBlank()) {
      throw new IllegalArgumentException("unit must not be null or blank");
    }
    if (timestamp == null) {
      throw new IllegalArgumentException("timestamp must not be null");
    }
    this.sensorId = sensorId;
    this.sensorType = sensorType;
    this.value = value;
    this.unit = unit;
    this.timestamp = timestamp;
  }

  /**
   * Factory method for creating a reading with the current timestamp.
   *
   * @param sensorId   unique identifier for the sensor
   * @param sensorType the type of measurement
   * @param value      the measured value
   * @param unit       the measurement unit
   * @return a new SensorReading with timestamp set to now
   */
  @Nonnull
  public static SensorReading of(@Nonnull String sensorId, @Nonnull SensorType sensorType,
      double value, @Nonnull String unit) {
    return new SensorReading(sensorId, sensorType, value, unit, Instant.now());
  }

  /** Returns the unique identifier for the sensor that produced this reading. */
  @Nonnull
  public String getSensorId() {
    return sensorId;
  }

  /** Returns the type of measurement (PRESSURE, TEMPERATURE, etc.). */
  @Nonnull
  public SensorType getSensorType() {
    return sensorType;
  }

  /** Returns the measured value in the sensor's native unit. */
  public double getValue() {
    return value;
  }

  /** Returns the measurement unit (e.g., "PSI", "F", "GPM"). */
  @Nonnull
  public String getUnit() {
    return unit;
  }

  /** Returns the timestamp when the reading was taken. */
  @Nonnull
  public Instant getTimestamp() {
    return timestamp;
  }

  /** {@inheritDoc} Equality is based on all fields. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SensorReading that = (SensorReading) o;
    return Double.compare(that.value, value) == 0
        && Objects.equals(sensorId, that.sensorId)
        && sensorType == that.sensorType
        && Objects.equals(unit, that.unit)
        && Objects.equals(timestamp, that.timestamp);
  }

  /** {@inheritDoc} Hash is computed from all fields. */
  @Override
  public int hashCode() {
    return Objects.hash(sensorId, sensorType, value, unit, timestamp);
  }

  /** {@inheritDoc} Returns a human-readable summary of this reading. */
  @Override
  public String toString() {
    return String.format("SensorReading{sensor=%s, type=%s, value=%.1f %s, time=%s}",
        sensorId, sensorType, value, unit, timestamp);
  }
}
