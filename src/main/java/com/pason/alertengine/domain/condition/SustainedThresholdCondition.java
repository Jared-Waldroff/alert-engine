package com.pason.alertengine.domain.condition;

import com.pason.alertengine.domain.model.ComparisonOperator;
import com.pason.alertengine.domain.model.SensorReading;
import jakarta.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Evaluates whether a sensor value has been above (or below) a threshold
 * for a sustained period of time.
 *
 * <p>Unlike {@link ThresholdExceededCondition} which triggers on a single reading,
 * this condition only triggers when ALL readings within the sustained duration
 * window breach the threshold. This prevents false alarms from transient spikes
 * that quickly return to normal.</p>
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>Temperature > 275°F for 30 seconds — cooling system struggling</li>
 *   <li>Gas level > 15% LEL for 60 seconds — persistent gas migration</li>
 * </ul>
 *
 * <p>Requires sufficient historical readings to cover the sustained duration.
 * If the history window is shorter than the required duration, the condition
 * does not trigger (safe default: we cannot confirm the threshold was
 * sustained without enough data).</p>
 *
 * @see AlertCondition
 * @see ThresholdExceededCondition
 * @see RateOfChangeCondition
 * @see OutOfRangeCondition
 */
public final class SustainedThresholdCondition implements AlertCondition {

  public static final String TYPE = "SUSTAINED_THRESHOLD";

  private final double threshold;
  private final ComparisonOperator operator;
  private final Duration sustainedDuration;

  /**
   * Creates a sustained threshold condition.
   *
   * @param threshold         the threshold value
   * @param operator          the comparison operator
   * @param sustainedDuration how long the threshold must be breached continuously
   * @throws IllegalArgumentException if operator or sustainedDuration is null,
   *                                  or if sustainedDuration is not positive
   */
  public SustainedThresholdCondition(double threshold, ComparisonOperator operator,
      Duration sustainedDuration) {
    if (operator == null) {
      throw new IllegalArgumentException("operator must not be null");
    }
    if (sustainedDuration == null || sustainedDuration.isNegative()
        || sustainedDuration.isZero()) {
      throw new IllegalArgumentException("sustainedDuration must be positive");
    }
    this.threshold = threshold;
    this.operator = operator;
    this.sustainedDuration = sustainedDuration;
  }

  /**
   * Evaluates whether the threshold has been continuously breached for the
   * configured duration.
   *
   * <p>Algorithm:</p>
   * <ol>
   *   <li>Check if the current reading breaches the threshold</li>
   *   <li>Calculate the time window: current timestamp minus sustained duration</li>
   *   <li>Collect all historical readings within that window</li>
   *   <li>Verify ALL readings in the window also breach the threshold</li>
   *   <li>Verify the history covers the full sustained duration</li>
   * </ol>
   *
   * @param reading       the current sensor reading
   * @param recentHistory recent readings ordered oldest-first
   * @return {@code true} if the threshold has been sustained for the required duration
   */
  @Override
  public boolean evaluate(@Nonnull SensorReading reading,
      @Nonnull List<SensorReading> recentHistory) {
    // Current reading must breach the threshold
    if (!operator.evaluate(reading.getValue(), threshold)) {
      return false;
    }

    if (recentHistory.isEmpty()) {
      return false;
    }

    Instant windowStart = reading.getTimestamp().minus(sustainedDuration);

    // Get readings within the sustained duration window
    List<SensorReading> windowReadings = recentHistory.stream()
        .filter(r -> !r.getTimestamp().isBefore(windowStart))
        .toList();

    // Need at least one reading in the window to confirm sustained breach
    if (windowReadings.isEmpty()) {
      return false;
    }

    // Verify the earliest reading in the window is old enough to cover the duration
    Instant earliestInWindow = windowReadings.get(0).getTimestamp();
    if (earliestInWindow.isAfter(windowStart.plus(Duration.ofSeconds(2)))) {
      // History doesn't go back far enough to confirm the full duration
      return false;
    }

    // All readings in the window must breach the threshold
    return windowReadings.stream()
        .allMatch(r -> operator.evaluate(r.getValue(), threshold));
  }

  @Override
  public String describe() {
    return String.format("Value %s %.1f for at least %d seconds",
        operator.getSymbol(), threshold, sustainedDuration.getSeconds());
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

  /** Returns the duration for which the threshold must be continuously breached. */
  public Duration getSustainedDuration() {
    return sustainedDuration;
  }

  /** {@inheritDoc} Equality is based on threshold, operator, and sustained duration. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SustainedThresholdCondition that = (SustainedThresholdCondition) o;
    return Double.compare(that.threshold, threshold) == 0
        && operator == that.operator
        && Objects.equals(sustainedDuration, that.sustainedDuration);
  }

  /** {@inheritDoc} Hash is computed from threshold, operator, and sustained duration. */
  @Override
  public int hashCode() {
    return Objects.hash(threshold, operator, sustainedDuration);
  }

  /** {@inheritDoc} Returns a human-readable summary of this condition. */
  @Override
  public String toString() {
    return String.format(
        "SustainedThresholdCondition{threshold=%.1f, operator=%s, duration=%ds}",
        threshold, operator, sustainedDuration.getSeconds());
  }
}
