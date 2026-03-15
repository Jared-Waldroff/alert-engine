package com.pason.alertengine.domain.condition;

import com.pason.alertengine.domain.model.ComparisonOperator;
import com.pason.alertengine.domain.model.SensorReading;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * Evaluates whether a sensor reading breaches a fixed threshold.
 *
 * <p>This is the simplest alert condition: it compares the reading's value
 * against a configured threshold using a comparison operator (greater than
 * or less than). It does not consider historical readings.</p>
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>Pressure > 3000 PSI — triggers when pressure exceeds 3000</li>
 *   <li>Flow rate < 200 GPM — triggers when flow drops below 200</li>
 * </ul>
 *
 * <p>The comparison uses strict inequality: a value exactly equal to the threshold
 * does NOT trigger the condition. This prevents false alarms from sensors
 * that fluctuate at the boundary value due to noise.</p>
 *
 * <p>This implements the Strategy pattern — see {@link AlertCondition}
 * for the interface contract.</p>
 *
 * @see AlertCondition
 * @see RateOfChangeCondition
 * @see SustainedThresholdCondition
 * @see OutOfRangeCondition
 */
public final class ThresholdExceededCondition implements AlertCondition {

  public static final String TYPE = "THRESHOLD_EXCEEDED";

  private final double threshold;
  private final ComparisonOperator operator;

  /**
   * Creates a threshold condition.
   *
   * @param threshold the threshold value to compare against
   * @param operator  the comparison operator (GREATER_THAN or LESS_THAN)
   * @throws IllegalArgumentException if operator is null
   */
  public ThresholdExceededCondition(double threshold, ComparisonOperator operator) {
    if (operator == null) {
      throw new IllegalArgumentException("operator must not be null");
    }
    this.threshold = threshold;
    this.operator = operator;
  }

  /**
   * Evaluates whether the given reading breaches this condition's threshold.
   *
   * <p>The comparison is strict: a value exactly equal to the threshold
   * does NOT trigger the condition. This prevents false alarms from sensors
   * that fluctuate at the boundary.</p>
   *
   * @param reading       the current sensor reading to evaluate
   * @param recentHistory recent readings for this sensor (unused by this condition,
   *                      included to satisfy the {@link AlertCondition} interface)
   * @return {@code true} if the reading's value breaches the threshold
   */
  @Override
  public boolean evaluate(@Nonnull SensorReading reading,
      @Nonnull List<SensorReading> recentHistory) {
    // Strict inequality: a reading exactly at the threshold is considered safe.
    // On a drilling rig, sensor noise can cause readings to hover around the threshold.
    // Using >= would cause alert storms when a sensor oscillates at the boundary value.
    return operator.evaluate(reading.getValue(), threshold);
  }

  @Override
  public String describe() {
    return String.format("Value %s %.1f", operator.getSymbol(), threshold);
  }

  @Override
  public String getType() {
    return TYPE;
  }

  /** Returns the threshold value that readings are compared against. */
  public double getThreshold() {
    return threshold;
  }

  /** Returns the comparison operator (GREATER_THAN or LESS_THAN). */
  public ComparisonOperator getOperator() {
    return operator;
  }

  /** {@inheritDoc} Equality is based on threshold and operator. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThresholdExceededCondition that = (ThresholdExceededCondition) o;
    return Double.compare(that.threshold, threshold) == 0 && operator == that.operator;
  }

  /** {@inheritDoc} Hash is computed from threshold and operator. */
  @Override
  public int hashCode() {
    return Objects.hash(threshold, operator);
  }

  /** {@inheritDoc} Returns a human-readable summary of this condition. */
  @Override
  public String toString() {
    return String.format("ThresholdExceededCondition{threshold=%.1f, operator=%s}",
        threshold, operator);
  }
}
