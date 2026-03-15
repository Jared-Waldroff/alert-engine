package com.pason.alertengine.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * An immutable alert generated when a sensor reading breaches a rule's condition.
 *
 * <p>Alerts are the output of the alert engine. Each alert captures the rule
 * that was triggered, the reading that caused it, and a human-readable message
 * describing what happened. Alerts are dispatched to all registered
 * {@link com.pason.alertengine.domain.dispatch.AlertDispatcher} implementations.</p>
 *
 * <p>Use the {@link Builder} to construct alerts:</p>
 * <pre>{@code
 * Alert alert = Alert.builder()
 *     .ruleName("Critical Pressure")
 *     .severity(AlertSeverity.CRITICAL)
 *     .sensorId("PRESSURE-001")
 *     .sensorType(SensorType.PRESSURE)
 *     .triggerValue(3500.0)
 *     .threshold(3000.0)
 *     .conditionDescription("Value > 3000.0 PSI")
 *     .message("Pressure reading of 3500.0 PSI exceeds threshold of 3000.0 PSI")
 *     .build();
 * }</pre>
 */
public final class Alert {

  private final String id;
  private final String ruleName;
  private final AlertSeverity severity;
  private final String sensorId;
  private final SensorType sensorType;
  private final double triggerValue;
  private final double threshold;
  private final String conditionDescription;
  private final String message;
  private final Instant timestamp;

  private Alert(Builder builder) {
    this.id = builder.id;
    this.ruleName = builder.ruleName;
    this.severity = builder.severity;
    this.sensorId = builder.sensorId;
    this.sensorType = builder.sensorType;
    this.triggerValue = builder.triggerValue;
    this.threshold = builder.threshold;
    this.conditionDescription = builder.conditionDescription;
    this.message = builder.message;
    this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
  }

  /**
   * Creates a new {@link Builder} for constructing an Alert.
   *
   * @return a new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Returns the unique alert ID (UUID). */
  public String getId() {
    return id;
  }

  /** Returns the name of the rule that triggered this alert. */
  public String getRuleName() {
    return ruleName;
  }

  /** Returns the severity level of this alert. */
  public AlertSeverity getSeverity() {
    return severity;
  }

  /** Returns the ID of the sensor whose reading triggered this alert. */
  public String getSensorId() {
    return sensorId;
  }

  /** Returns the sensor type that was being monitored. */
  public SensorType getSensorType() {
    return sensorType;
  }

  /** Returns the sensor value that triggered the alert. */
  public double getTriggerValue() {
    return triggerValue;
  }

  /** Returns the threshold value that was breached. */
  public double getThreshold() {
    return threshold;
  }

  /** Returns a human-readable description of the condition that was violated. */
  public String getConditionDescription() {
    return conditionDescription;
  }

  /** Returns a human-readable alert message describing what happened. */
  public String getMessage() {
    return message;
  }

  /** Returns the timestamp when the alert was generated. */
  public Instant getTimestamp() {
    return timestamp;
  }

  /** {@inheritDoc} Equality is based on the alert ID. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Alert alert = (Alert) o;
    return Objects.equals(id, alert.id);
  }

  /** {@inheritDoc} Hash is computed from the alert ID. */
  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  /** {@inheritDoc} Returns a human-readable summary of this alert. */
  @Override
  public String toString() {
    return String.format("Alert{rule=%s, severity=%s, sensor=%s, value=%.1f, message=%s}",
        ruleName, severity, sensorId, triggerValue, message);
  }

  /** Builder for constructing immutable {@link Alert} instances. */
  public static class Builder {

    private String id;
    private String ruleName;
    private AlertSeverity severity;
    private String sensorId;
    private SensorType sensorType;
    private double triggerValue;
    private double threshold;
    private String conditionDescription;
    private String message;
    private Instant timestamp;

    /** Sets the alert ID. If not provided, a UUID is generated at build time. */
    public Builder id(String id) {
      this.id = id;
      return this;
    }

    /** Sets the name of the rule that triggered this alert. */
    public Builder ruleName(String ruleName) {
      this.ruleName = ruleName;
      return this;
    }

    /** Sets the severity level of this alert. */
    public Builder severity(AlertSeverity severity) {
      this.severity = severity;
      return this;
    }

    /** Sets the ID of the sensor whose reading triggered this alert. */
    public Builder sensorId(String sensorId) {
      this.sensorId = sensorId;
      return this;
    }

    /** Sets the sensor type that was being monitored. */
    public Builder sensorType(SensorType sensorType) {
      this.sensorType = sensorType;
      return this;
    }

    /** Sets the sensor value that triggered the alert. */
    public Builder triggerValue(double triggerValue) {
      this.triggerValue = triggerValue;
      return this;
    }

    /** Sets the threshold value that was breached. */
    public Builder threshold(double threshold) {
      this.threshold = threshold;
      return this;
    }

    /** Sets a human-readable description of the condition that was violated. */
    public Builder conditionDescription(String conditionDescription) {
      this.conditionDescription = conditionDescription;
      return this;
    }

    /** Sets the human-readable alert message. */
    public Builder message(String message) {
      this.message = message;
      return this;
    }

    /** Sets the timestamp; defaults to now if not provided. */
    public Builder timestamp(Instant timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    /**
     * Builds the alert instance. Generates a UUID if no ID was provided.
     *
     * @return a new immutable Alert
     * @throws IllegalStateException if required fields are missing
     */
    public Alert build() {
      if (ruleName == null || ruleName.isBlank()) {
        throw new IllegalStateException("ruleName is required");
      }
      if (severity == null) {
        throw new IllegalStateException("severity is required");
      }
      if (sensorId == null || sensorId.isBlank()) {
        throw new IllegalStateException("sensorId is required");
      }
      if (sensorType == null) {
        throw new IllegalStateException("sensorType is required");
      }
      if (conditionDescription == null || conditionDescription.isBlank()) {
        throw new IllegalStateException("conditionDescription is required");
      }
      if (message == null || message.isBlank()) {
        throw new IllegalStateException("message is required");
      }
      if (id == null) {
        id = java.util.UUID.randomUUID().toString();
      }
      return new Alert(this);
    }
  }
}
