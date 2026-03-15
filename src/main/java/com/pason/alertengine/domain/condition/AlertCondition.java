package com.pason.alertengine.domain.condition;

import com.pason.alertengine.domain.model.SensorReading;
import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * Strategy interface for evaluating whether a sensor reading triggers an alert.
 *
 * <p>This is the core extension point of the alert engine. Each implementation
 * defines a different type of threshold evaluation logic. The engine evaluates
 * all conditions associated with active rules against each incoming reading.</p>
 *
 * <p>Implementations:</p>
 * <ul>
 *   <li>{@link ThresholdExceededCondition} — simple value comparison</li>
 *   <li>{@link RateOfChangeCondition} — detects rapid value changes</li>
 *   <li>{@link SustainedThresholdCondition} — value above threshold for a duration</li>
 *   <li>{@link OutOfRangeCondition} — value outside a valid range</li>
 * </ul>
 *
 * <p>Implementations must be thread-safe and stateless (all state is in the
 * condition's constructor parameters). The {@code recentHistory} parameter
 * provides recent readings for conditions that need historical context.</p>
 *
 * @see com.pason.alertengine.engine.AlertEngine
 */
public interface AlertCondition {

  /**
   * Evaluates whether the given reading triggers this condition.
   *
   * @param reading       the current sensor reading to evaluate
   * @param recentHistory recent readings for the same sensor, ordered oldest-first;
   *                      may be empty if no history is available
   * @return {@code true} if the condition is triggered
   */
  boolean evaluate(@Nonnull SensorReading reading, @Nonnull List<SensorReading> recentHistory);

  /**
   * Returns a human-readable description of this condition.
   *
   * <p>Used in alert messages and the dashboard to explain what the condition
   * checks. Example: "Value > 3000.0 PSI" or "Rate of change > 50.0/min".</p>
   *
   * @return a human-readable description
   */
  @Nonnull
  String describe();

  /**
   * Returns the condition type identifier used for serialization.
   *
   * <p>This string is stored in the database and used to reconstruct
   * the correct condition implementation when loading rules.</p>
   *
   * @return the condition type identifier (e.g., "THRESHOLD_EXCEEDED")
   */
  @Nonnull
  String getType();
}
