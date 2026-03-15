package com.pason.alertengine.domain.condition;

import com.pason.alertengine.domain.model.SensorReading;
import jakarta.annotation.Nonnull;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Evaluates whether a sensor value is changing too rapidly.
 *
 * <p>Detects sudden spikes or drops by calculating the rate of change
 * between the current reading and the most recent historical reading.
 * The rate is expressed as change per second.</p>
 *
 * <p>This condition requires at least one historical reading to evaluate.
 * If no history is available, the condition does not trigger (safe default).</p>
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>Pressure changing more than 100 PSI/sec — indicates a kick or blowout</li>
 *   <li>Temperature changing more than 20°F/sec — indicates cooling system failure</li>
 * </ul>
 *
 * <p>This implements the Strategy pattern — see {@link AlertCondition}
 * for the interface contract.</p>
 *
 * @see AlertCondition
 * @see ThresholdExceededCondition
 * @see SustainedThresholdCondition
 * @see OutOfRangeCondition
 */
public final class RateOfChangeCondition implements AlertCondition {

  public static final String TYPE = "RATE_OF_CHANGE";

  private final double maxRatePerSecond;

  /**
   * Creates a rate-of-change condition.
   *
   * @param maxRatePerSecond the maximum allowed rate of change per second;
   *                         triggers when the absolute rate exceeds this value
   * @throws IllegalArgumentException if maxRatePerSecond is not positive
   */
  public RateOfChangeCondition(double maxRatePerSecond) {
    if (maxRatePerSecond <= 0) {
      throw new IllegalArgumentException("maxRatePerSecond must be positive, got: "
          + maxRatePerSecond);
    }
    this.maxRatePerSecond = maxRatePerSecond;
  }

  /**
   * Evaluates whether the rate of change between the current reading and the
   * most recent historical reading exceeds the configured threshold.
   *
   * <p>Uses the absolute value of the rate, so both rapid increases and
   * rapid decreases trigger the condition. Returns false if there is no
   * history to compare against (safe default for first readings).</p>
   *
   * @param reading       the current sensor reading
   * @param recentHistory recent readings ordered oldest-first; needs at least 1 entry
   * @return {@code true} if the rate of change exceeds the max rate
   */
  @Override
  public boolean evaluate(@Nonnull SensorReading reading,
      @Nonnull List<SensorReading> recentHistory) {
    if (recentHistory.isEmpty()) {
      // No history to compare against — cannot determine rate.
      // Safe default: do not trigger an alert on the first reading.
      return false;
    }

    SensorReading previous = recentHistory.get(recentHistory.size() - 1);
    Duration timeDelta = Duration.between(previous.getTimestamp(), reading.getTimestamp());

    // Guard against zero or negative time deltas from clock skew
    long deltaMillis = timeDelta.toMillis();
    if (deltaMillis <= 0) {
      return false;
    }

    double valueDelta = Math.abs(reading.getValue() - previous.getValue());
    double ratePerSecond = valueDelta / (deltaMillis / 1000.0);

    return ratePerSecond > maxRatePerSecond;
  }

  @Override
  public String describe() {
    return String.format("Rate of change > %.1f/sec", maxRatePerSecond);
  }

  @Override
  public String getType() {
    return TYPE;
  }

  /** Returns the maximum allowed rate of change per second. */
  public double getMaxRatePerSecond() {
    return maxRatePerSecond;
  }

  /** {@inheritDoc} Equality is based on the max rate per second. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RateOfChangeCondition that = (RateOfChangeCondition) o;
    return Double.compare(that.maxRatePerSecond, maxRatePerSecond) == 0;
  }

  /** {@inheritDoc} Hash is computed from the max rate per second. */
  @Override
  public int hashCode() {
    return Objects.hash(maxRatePerSecond);
  }

  /** {@inheritDoc} Returns a human-readable summary of this condition. */
  @Override
  public String toString() {
    return String.format("RateOfChangeCondition{maxRate=%.1f/sec}", maxRatePerSecond);
  }
}
