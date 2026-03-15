package com.pason.alertengine.engine;

import com.pason.alertengine.domain.condition.AlertCondition;
import com.pason.alertengine.domain.condition.OutOfRangeCondition;
import com.pason.alertengine.domain.condition.ThresholdExceededCondition;
import com.pason.alertengine.domain.dispatch.AlertDispatcher;
import com.pason.alertengine.domain.model.Alert;
import com.pason.alertengine.domain.model.AlertRule;
import com.pason.alertengine.domain.model.SensorReading;
import jakarta.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Core processing engine that evaluates sensor readings against alert rules.
 *
 * <p>Processing pipeline for each incoming reading:</p>
 * <ol>
 *   <li>Record the reading in the sliding window for historical context</li>
 *   <li>Filter active rules matching the reading's sensor type</li>
 *   <li>Evaluate each matching rule's condition against the reading</li>
 *   <li>For any triggered condition, create an Alert and dispatch to all observers</li>
 *   <li>Update processing metrics (readings count, alerts count)</li>
 * </ol>
 *
 * <p>Thread safety: the engine uses concurrent collections for rules and
 * dispatchers, and an atomic counter for metrics. Multiple sensor streams
 * can safely call {@link #processReading(SensorReading)} concurrently.</p>
 *
 * <p>Design patterns:</p>
 * <ul>
 *   <li>Strategy: {@link AlertCondition} implementations are interchangeable
 *       evaluation algorithms selected per-rule</li>
 *   <li>Observer: {@link AlertDispatcher} implementations are notified
 *       whenever an alert is triggered</li>
 * </ul>
 *
 * @see AlertCondition
 * @see AlertDispatcher
 * @see ReadingWindow
 */
@Component
public class AlertEngine {

  private static final Logger log = LoggerFactory.getLogger(AlertEngine.class);

  private final CopyOnWriteArrayList<AlertRule> activeRules;
  private final List<AlertDispatcher> dispatchers;
  private final ReadingWindow readingWindow;
  private final AtomicLong readingsProcessed;
  private final AtomicLong alertsTriggered;
  private final Instant startTime;

  /**
   * Creates an alert engine with the given dispatchers.
   *
   * <p>Rules are loaded separately via {@link #loadRules(List)} after construction.
   * Dispatchers are injected by Spring and are available immediately.</p>
   *
   * @param dispatchers the alert dispatchers to notify when alerts trigger
   */
  @Autowired
  public AlertEngine(@Nonnull List<AlertDispatcher> dispatchers) {
    this.activeRules = new CopyOnWriteArrayList<>();
    this.dispatchers = dispatchers != null ? dispatchers : List.of();
    this.readingWindow = new ReadingWindow();
    this.readingsProcessed = new AtomicLong(0);
    this.alertsTriggered = new AtomicLong(0);
    this.startTime = Instant.now();
  }

  /**
   * Creates an alert engine with explicit rules and dispatchers (for testing).
   *
   * @param rules       the alert rules to evaluate
   * @param dispatchers the dispatchers to notify
   */
  public AlertEngine(@Nonnull List<AlertRule> rules, @Nonnull List<AlertDispatcher> dispatchers) {
    this.activeRules = new CopyOnWriteArrayList<>(rules);
    this.dispatchers = dispatchers != null ? dispatchers : List.of();
    this.readingWindow = new ReadingWindow();
    this.readingsProcessed = new AtomicLong(0);
    this.alertsTriggered = new AtomicLong(0);
    this.startTime = Instant.now();
  }

  /**
   * Processes a single sensor reading against all active rules.
   *
   * <p>Returns a list of alerts that were triggered by this reading.
   * An empty list means no rules were triggered. Each triggered rule
   * produces exactly one alert.</p>
   *
   * @param reading the sensor reading to evaluate
   * @return list of triggered alerts (may be empty)
   */
  @Nonnull
  public List<Alert> processReading(@Nonnull SensorReading reading) {
    log.debug("Processing reading: sensor={}, type={}, value={}",
        reading.getSensorId(), reading.getSensorType(), reading.getValue());

    List<SensorReading> history = readingWindow.getRecentReadings(reading.getSensorId());
    readingWindow.addReading(reading);
    readingsProcessed.incrementAndGet();

    List<Alert> triggeredAlerts = new ArrayList<>();

    for (AlertRule rule : activeRules) {
      if (!rule.isEnabled()) {
        continue;
      }
      if (rule.getSensorType() != reading.getSensorType()) {
        continue;
      }

      try {
        if (rule.getCondition().evaluate(reading, history)) {
          Alert alert = createAlert(rule, reading);
          triggeredAlerts.add(alert);
          alertsTriggered.incrementAndGet();
          dispatchAlert(alert);

          log.info("Alert triggered: rule={}, sensor={}, value={}, severity={}",
              rule.getName(), reading.getSensorId(),
              reading.getValue(), rule.getSeverity());
        }
      } catch (Exception e) {
        // Rule evaluation failure should not stop processing other rules
        log.warn("Rule evaluation failed: rule={}, sensor={}, error={}",
            rule.getName(), reading.getSensorId(), e.getMessage());
      }
    }

    return Collections.unmodifiableList(triggeredAlerts);
  }

  /**
   * Replaces all active rules with the given list.
   *
   * @param rules the new set of rules to evaluate
   */
  public synchronized void loadRules(@Nonnull List<AlertRule> rules) {
    // Atomic swap: replace all rules in one operation to avoid a window
    // where concurrent processReading() calls see an empty rules list
    CopyOnWriteArrayList<AlertRule> snapshot = new CopyOnWriteArrayList<>(rules);
    activeRules.clear();
    activeRules.addAll(snapshot);
    log.info("Loaded {} alert rules", rules.size());
  }

  /**
   * Adds a single rule to the engine.
   *
   * @param rule the rule to add
   */
  public void addRule(@Nonnull AlertRule rule) {
    activeRules.add(rule);
    log.info("Added rule: {}", rule.getName());
  }

  /**
   * Removes a rule by its ID.
   *
   * @param ruleId the ID of the rule to remove
   * @return true if a rule was removed
   */
  public boolean removeRule(@Nonnull Long ruleId) {
    boolean removed = activeRules.removeIf(
        r -> r.getId().isPresent() && ruleId.equals(r.getId().get()));
    if (removed) {
      log.info("Removed rule with ID: {}", ruleId);
    }
    return removed;
  }

  /** Returns an unmodifiable view of the active rules. */
  @Nonnull
  public List<AlertRule> getActiveRules() {
    return Collections.unmodifiableList(activeRules);
  }

  /** Returns the total number of readings processed since engine start. */
  public long getReadingsProcessed() {
    return readingsProcessed.get();
  }

  /** Returns the total number of alerts triggered since engine start. */
  public long getAlertsTriggered() {
    return alertsTriggered.get();
  }

  /**
   * Returns the duration since the engine was started.
   *
   * @return uptime as a {@link Duration}
   */
  @Nonnull
  public Duration getUptime() {
    return Duration.between(startTime, Instant.now());
  }

  /** Resets processing metrics to zero. */
  public void resetMetrics() {
    readingsProcessed.set(0);
    alertsTriggered.set(0);
  }

  private Alert createAlert(AlertRule rule, SensorReading reading) {
    String message = String.format("%s reading of %.1f %s triggered rule '%s' (%s)",
        reading.getSensorType(),
        reading.getValue(),
        reading.getUnit(),
        rule.getName(),
        rule.getCondition().describe());

    return Alert.builder()
        .ruleName(rule.getName())
        .severity(rule.getSeverity())
        .sensorId(reading.getSensorId())
        .sensorType(reading.getSensorType())
        .triggerValue(reading.getValue())
        .threshold(extractThreshold(rule.getCondition()))
        .conditionDescription(rule.getCondition().describe())
        .message(message)
        .timestamp(reading.getTimestamp())
        .build();
  }

  private void dispatchAlert(Alert alert) {
    for (AlertDispatcher dispatcher : dispatchers) {
      try {
        dispatcher.dispatch(alert);
      } catch (Exception e) {
        // Dispatcher failure should not prevent other dispatchers from receiving the alert
        log.error("Dispatcher '{}' failed for alert {}: {}",
            dispatcher.getName(), alert.getId(), e.getMessage());
      }
    }
  }

  /**
   * Extracts the numeric threshold from a condition for alert reporting.
   * Returns 0 for condition types that do not have a single threshold value.
   */
  private double extractThreshold(AlertCondition condition) {
    if (condition instanceof ThresholdExceededCondition tc) {
      return tc.getThreshold();
    }
    if (condition instanceof OutOfRangeCondition oc) {
      return oc.getMin(); // Report the boundary that was breached
    }
    return 0;
  }
}
