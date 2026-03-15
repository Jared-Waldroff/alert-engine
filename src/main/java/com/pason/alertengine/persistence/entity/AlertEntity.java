package com.pason.alertengine.persistence.entity;

import com.pason.alertengine.domain.model.AlertSeverity;
import com.pason.alertengine.domain.model.SensorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA entity for persisted alert records.
 *
 * <p>Stores the complete history of triggered alerts, enabling
 * historical analysis and dashboard display. Each record captures
 * all details needed to understand why the alert was triggered.</p>
 */
@Entity
@Table(name = "alerts")
public class AlertEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, name = "alert_id", unique = true)
  private String alertId;

  @Column(nullable = false, name = "rule_name")
  private String ruleName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AlertSeverity severity;

  @Column(nullable = false, name = "sensor_id")
  private String sensorId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "sensor_type")
  private SensorType sensorType;

  @Column(nullable = false, name = "trigger_value")
  private double triggerValue;

  @Column(nullable = false)
  private double threshold;

  @Column(name = "condition_description")
  private String conditionDescription;

  @Column(nullable = false, length = 500)
  private String message;

  @Column(nullable = false, name = "alert_timestamp")
  private Instant timestamp;

  @Column(nullable = false, name = "created_at")
  private Instant createdAt = Instant.now();

  /** Returns the auto-generated primary key. */
  public Long getId() {
    return id;
  }

  /** Sets the primary key. */
  public void setId(Long id) {
    this.id = id;
  }

  /** Returns the domain-level alert UUID. */
  public String getAlertId() {
    return alertId;
  }

  /** Sets the domain-level alert UUID. */
  public void setAlertId(String alertId) {
    this.alertId = alertId;
  }

  /** Returns the name of the rule that triggered this alert. */
  public String getRuleName() {
    return ruleName;
  }

  /** Sets the name of the rule that triggered this alert. */
  public void setRuleName(String ruleName) {
    this.ruleName = ruleName;
  }

  /** Returns the alert severity level. */
  public AlertSeverity getSeverity() {
    return severity;
  }

  /** Sets the alert severity level. */
  public void setSeverity(AlertSeverity severity) {
    this.severity = severity;
  }

  /** Returns the ID of the sensor that triggered this alert. */
  public String getSensorId() {
    return sensorId;
  }

  /** Sets the ID of the sensor that triggered this alert. */
  public void setSensorId(String sensorId) {
    this.sensorId = sensorId;
  }

  /** Returns the sensor type that was being monitored. */
  public SensorType getSensorType() {
    return sensorType;
  }

  /** Sets the sensor type that was being monitored. */
  public void setSensorType(SensorType sensorType) {
    this.sensorType = sensorType;
  }

  /** Returns the sensor value that triggered the alert. */
  public double getTriggerValue() {
    return triggerValue;
  }

  /** Sets the sensor value that triggered the alert. */
  public void setTriggerValue(double triggerValue) {
    this.triggerValue = triggerValue;
  }

  /** Returns the threshold value that was breached. */
  public double getThreshold() {
    return threshold;
  }

  /** Sets the threshold value that was breached. */
  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }

  /** Returns a human-readable description of the condition that was violated. */
  public String getConditionDescription() {
    return conditionDescription;
  }

  /** Sets the condition description. */
  public void setConditionDescription(String conditionDescription) {
    this.conditionDescription = conditionDescription;
  }

  /** Returns the human-readable alert message. */
  public String getMessage() {
    return message;
  }

  /** Sets the human-readable alert message. */
  public void setMessage(String message) {
    this.message = message;
  }

  /** Returns the timestamp when the alert was triggered. */
  public Instant getTimestamp() {
    return timestamp;
  }

  /** Sets the alert trigger timestamp. */
  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  /** Returns the timestamp when this record was persisted. */
  public Instant getCreatedAt() {
    return createdAt;
  }

  /** Sets the persistence timestamp. */
  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
