package com.pason.alertengine.persistence.entity;

import com.pason.alertengine.domain.model.SensorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA entity for persisted sensor readings.
 *
 * <p>Stores the last N readings for dashboard display and historical
 * reference. The sliding window in the engine handles real-time processing;
 * this entity provides persistence for the dashboard's live feed view.</p>
 */
@Entity
@Table(name = "sensor_readings", indexes = {
    @Index(name = "idx_readings_sensor_time", columnList = "sensor_id, reading_timestamp DESC")
})
public class SensorReadingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, name = "sensor_id")
  private String sensorId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "sensor_type")
  private SensorType sensorType;

  @Column(nullable = false)
  private double value;

  @Column(nullable = false)
  private String unit;

  @Column(nullable = false, name = "reading_timestamp")
  private Instant timestamp;

  /** Returns the auto-generated primary key. */
  public Long getId() {
    return id;
  }

  /** Sets the primary key. */
  public void setId(Long id) {
    this.id = id;
  }

  /** Returns the unique identifier of the sensor. */
  public String getSensorId() {
    return sensorId;
  }

  /** Sets the unique identifier of the sensor. */
  public void setSensorId(String sensorId) {
    this.sensorId = sensorId;
  }

  /** Returns the type of measurement. */
  public SensorType getSensorType() {
    return sensorType;
  }

  /** Sets the type of measurement. */
  public void setSensorType(SensorType sensorType) {
    this.sensorType = sensorType;
  }

  /** Returns the measured value. */
  public double getValue() {
    return value;
  }

  /** Sets the measured value. */
  public void setValue(double value) {
    this.value = value;
  }

  /** Returns the measurement unit (e.g., "PSI", "F"). */
  public String getUnit() {
    return unit;
  }

  /** Sets the measurement unit. */
  public void setUnit(String unit) {
    this.unit = unit;
  }

  /** Returns the timestamp when the reading was taken. */
  public Instant getTimestamp() {
    return timestamp;
  }

  /** Sets the reading timestamp. */
  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }
}
