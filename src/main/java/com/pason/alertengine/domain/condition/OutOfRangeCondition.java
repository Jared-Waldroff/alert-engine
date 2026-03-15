package com.pason.alertengine.domain.condition;

import com.pason.alertengine.domain.model.SensorReading;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * Evaluates whether a sensor reading falls outside a valid operating range.
 *
 * <p>This condition triggers when a reading's value is below the minimum
 * or above the maximum of the configured range. It is useful for detecting
 * both dangerously low and dangerously high values in a single rule.</p>
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>Flow rate outside 200-700 GPM — pump malfunction (too low) or line burst (too high)</li>
 *   <li>Rotary speed outside 30-180 RPM — motor stall or runaway</li>
 * </ul>
 *
 * <p>The range boundaries are inclusive: values exactly at the min or max
 * are considered within range and do not trigger the condition.</p>
 *
 * @see AlertCondition
 * @see ThresholdExceededCondition
 * @see RateOfChangeCondition
 * @see SustainedThresholdCondition
 */
public final class OutOfRangeCondition implements AlertCondition {

  public static final String TYPE = "OUT_OF_RANGE";

  private final double min;
  private final double max;

  /**
   * Creates an out-of-range condition.
   *
   * @param min the minimum acceptable value (inclusive)
   * @param max the maximum acceptable value (inclusive)
   * @throws IllegalArgumentException if min >= max
   */
  public OutOfRangeCondition(double min, double max) {
    if (min >= max) {
      throw new IllegalArgumentException(
          String.format("min (%.1f) must be less than max (%.1f)", min, max));
    }
    this.min = min;
    this.max = max;
  }

  /**
   * Evaluates whether the reading falls outside the configured range.
   *
   * <p>Values at the boundary (exactly min or exactly max) are considered
   * within range. This matches the behavior of {@link ThresholdExceededCondition}
   * which uses strict inequality to prevent boundary oscillation alerts.</p>
   *
   * @param reading       the current sensor reading
   * @param recentHistory recent readings (unused by this condition)
   * @return {@code true} if the value is strictly below min or strictly above max
   */
  @Override
  public boolean evaluate(@Nonnull SensorReading reading,
      @Nonnull List<SensorReading> recentHistory) {
    double value = reading.getValue();
    // Strict inequality: boundary values are safe
    return value < min || value > max;
  }

  @Override
  public String describe() {
    return String.format("Value outside range [%.1f, %.1f]", min, max);
  }

  @Override
  public String getType() {
    return TYPE;
  }

  /** Returns the minimum acceptable value (inclusive). */
  public double getMin() {
    return min;
  }

  /** Returns the maximum acceptable value (inclusive). */
  public double getMax() {
    return max;
  }

  /** {@inheritDoc} Equality is based on min and max range values. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OutOfRangeCondition that = (OutOfRangeCondition) o;
    return Double.compare(that.min, min) == 0 && Double.compare(that.max, max) == 0;
  }

  /** {@inheritDoc} Hash is computed from min and max range values. */
  @Override
  public int hashCode() {
    return Objects.hash(min, max);
  }

  /** {@inheritDoc} Returns a human-readable summary of this condition. */
  @Override
  public String toString() {
    return String.format("OutOfRangeCondition{min=%.1f, max=%.1f}", min, max);
  }
}
