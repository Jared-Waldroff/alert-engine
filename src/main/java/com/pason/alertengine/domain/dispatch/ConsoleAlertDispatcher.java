package com.pason.alertengine.domain.dispatch;

import com.pason.alertengine.domain.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Dispatches alerts by logging them to the console via SLF4J.
 *
 * <p>This is the simplest dispatcher and is always active. It provides
 * immediate visibility of alerts in the application logs, which is
 * especially useful during development and for the simulator demo.</p>
 *
 * <p>Log level is determined by alert severity:</p>
 * <ul>
 *   <li>CRITICAL — logged at ERROR level</li>
 *   <li>WARNING — logged at WARN level</li>
 *   <li>INFO — logged at INFO level</li>
 * </ul>
 */
@Component
@Order(1)
public class ConsoleAlertDispatcher implements AlertDispatcher {

  private static final Logger log = LoggerFactory.getLogger(ConsoleAlertDispatcher.class);

  /**
   * Logs the alert to the console at the appropriate severity level.
   *
   * @param alert the alert to log
   */
  @Override
  public void dispatch(Alert alert) {
    String logMessage = formatAlert(alert);

    switch (alert.getSeverity()) {
      case CRITICAL -> log.error("ALERT [CRITICAL] {}", logMessage);
      case WARNING -> log.warn("ALERT [WARNING] {}", logMessage);
      case INFO -> log.info("ALERT [INFO] {}", logMessage);
      default -> log.info("ALERT [UNKNOWN] {}", logMessage);
    }
  }

  /** {@inheritDoc} Returns {@code "Console"}. */
  @Override
  public String getName() {
    return "Console";
  }

  private String formatAlert(Alert alert) {
    return String.format("rule=%s sensor=%s value=%.1f threshold=%.1f message=\"%s\"",
        alert.getRuleName(),
        alert.getSensorId(),
        alert.getTriggerValue(),
        alert.getThreshold(),
        alert.getMessage());
  }
}
