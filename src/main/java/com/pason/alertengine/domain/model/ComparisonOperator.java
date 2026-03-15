package com.pason.alertengine.domain.model;

/**
 * Comparison operators used in threshold-based alert conditions.
 *
 * <p>Operators use strict inequality: a value exactly equal to the threshold
 * does NOT trigger the condition. This design choice prevents alert storms
 * when sensor readings oscillate at the boundary value due to noise.</p>
 */
public enum ComparisonOperator {

  GREATER_THAN(">") {
    @Override
    public boolean evaluate(double value, double threshold) {
      return value > threshold;
    }
  },

  LESS_THAN("<") {
    @Override
    public boolean evaluate(double value, double threshold) {
      return value < threshold;
    }
  };

  private final String symbol;

  ComparisonOperator(String symbol) {
    this.symbol = symbol;
  }

  /** Returns the mathematical symbol for this operator (e.g., ">"). */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Evaluates the comparison between a value and a threshold.
   *
   * @param value     the sensor reading value
   * @param threshold the configured threshold
   * @return true if the comparison holds
   */
  public abstract boolean evaluate(double value, double threshold);
}
