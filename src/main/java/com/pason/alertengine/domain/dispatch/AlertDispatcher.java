package com.pason.alertengine.domain.dispatch;

import com.pason.alertengine.domain.model.Alert;
import jakarta.annotation.Nonnull;

/**
 * Observer interface for receiving and dispatching triggered alerts.
 *
 * <p>This is the delivery extension point of the alert engine. Each implementation
 * defines a different channel for alert delivery. The engine notifies all registered
 * dispatchers whenever an alert is triggered.</p>
 *
 * <p>Implementations:</p>
 * <ul>
 *   <li>{@link ConsoleAlertDispatcher} — logs alerts to the console (stdout)</li>
 *   <li>{@link DatabaseAlertDispatcher} — persists alerts to the database</li>
 *   <li>{@link LogFileAlertDispatcher} — writes alerts to a structured log file</li>
 * </ul>
 *
 * <p>Implementations must be thread-safe. The engine may call {@code dispatch}
 * from multiple threads if readings are processed concurrently.</p>
 *
 * @see com.pason.alertengine.engine.AlertEngine
 */
public interface AlertDispatcher {

  /**
   * Dispatches an alert through this channel.
   *
   * <p>Implementations should handle their own errors gracefully. A failure
   * in one dispatcher should not prevent other dispatchers from receiving
   * the alert.</p>
   *
   * @param alert the alert to dispatch; never null
   */
  void dispatch(@Nonnull Alert alert);

  /**
   * Returns a human-readable name for this dispatcher.
   *
   * <p>Used in logging and the dashboard to identify the dispatch channel.</p>
   *
   * @return the dispatcher name (e.g., "Console", "Database", "LogFile")
   */
  @Nonnull
  String getName();
}
