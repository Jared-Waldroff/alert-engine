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
 * JPA entity for persisted alert rules.
 *
 * <p>The condition configuration is stored as JSON in the database,
 * allowing different condition types to have different parameters
 * without requiring schema changes. The condition type field determines
 * how to deserialize the JSON config back into a domain condition object.</p>
 *
 * @see com.pason.alertengine.domain.model.AlertRule
 */
@Entity
@Table(name = "alert_rules")
public class AlertRuleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "sensor_type")
  private SensorType sensorType;

  @Column(nullable = false, name = "condition_type")
  private String conditionType;

  /** JSON-serialized condition parameters. */
  @Column(nullable = false, name = "condition_config", columnDefinition = "JSON")
  private String conditionConfig;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AlertSeverity severity;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(nullable = false, updatable = false, name = "created_at")
  private Instant createdAt = Instant.now();

  /** Returns the auto-generated primary key. */
  public Long getId() {
    return id;
  }

  /** Sets the primary key. */
  public void setId(Long id) {
    this.id = id;
  }

  /** Returns the rule name. */
  public String getName() {
    return name;
  }

  /** Sets the rule name. */
  public void setName(String name) {
    this.name = name;
  }

  /** Returns the sensor type this rule applies to. */
  public SensorType getSensorType() {
    return sensorType;
  }

  /** Sets the sensor type this rule applies to. */
  public void setSensorType(SensorType sensorType) {
    this.sensorType = sensorType;
  }

  /** Returns the condition type identifier (e.g., "THRESHOLD_EXCEEDED"). */
  public String getConditionType() {
    return conditionType;
  }

  /** Sets the condition type identifier. */
  public void setConditionType(String conditionType) {
    this.conditionType = conditionType;
  }

  /** Returns the JSON-serialized condition parameters. */
  public String getConditionConfig() {
    return conditionConfig;
  }

  /** Sets the JSON-serialized condition parameters. */
  public void setConditionConfig(String conditionConfig) {
    this.conditionConfig = conditionConfig;
  }

  /** Returns the alert severity level. */
  public AlertSeverity getSeverity() {
    return severity;
  }

  /** Sets the alert severity level. */
  public void setSeverity(AlertSeverity severity) {
    this.severity = severity;
  }

  /** Returns whether this rule is active for evaluation. */
  public boolean isEnabled() {
    return enabled;
  }

  /** Sets whether this rule is active for evaluation. */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /** Returns the timestamp when this rule was created. */
  public Instant getCreatedAt() {
    return createdAt;
  }

  /** Sets the creation timestamp. */
  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
