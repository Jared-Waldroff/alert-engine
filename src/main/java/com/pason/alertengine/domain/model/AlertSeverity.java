package com.pason.alertengine.domain.model;

/**
 * Severity levels for alerts, ordered from least to most critical.
 *
 * <p>Severity determines the urgency of the response required:</p>
 * <ul>
 *   <li>{@code INFO} — Informational; no action required, logged for awareness.</li>
 *   <li>{@code WARNING} — Approaching dangerous levels; crew should monitor closely.</li>
 *   <li>{@code CRITICAL} — Immediate danger; requires immediate crew response.</li>
 * </ul>
 */
public enum AlertSeverity {

  INFO,
  WARNING,
  CRITICAL;

  /**
   * Returns true if this severity is at least as severe as the given severity.
   *
   * @param other the severity to compare against
   * @return true if this severity is equal to or more severe than other
   */
  public boolean isAtLeast(AlertSeverity other) {
    return this.ordinal() >= other.ordinal();
  }
}
