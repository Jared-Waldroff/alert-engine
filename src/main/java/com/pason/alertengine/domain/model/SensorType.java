package com.pason.alertengine.domain.model;

/**
 * Types of sensors found on a drilling rig.
 *
 * <p>Each sensor type measures a different physical property and has
 * different normal operating ranges. Alert rules are configured per
 * sensor type to account for these differences.</p>
 */
public enum SensorType {

  /** Drilling fluid pressure in PSI. Normal range: 2000-3000 PSI. */
  PRESSURE("PSI"),

  /** Downhole temperature in degrees Fahrenheit. Normal range: 150-250°F. */
  TEMPERATURE("°F"),

  /** Mud flow rate in gallons per minute. Normal range: 300-600 GPM. */
  FLOW_RATE("GPM"),

  /** Combustible gas concentration as percentage of LEL. Normal range: 0-10%. */
  GAS_LEVEL("%LEL"),

  /** Rotary table speed in revolutions per minute. Normal range: 50-150 RPM. */
  ROTARY_SPEED("RPM");

  private final String unit;

  SensorType(String unit) {
    this.unit = unit;
  }

  /** Returns the measurement unit for this sensor type (e.g., "PSI", "°F"). */
  public String getUnit() {
    return unit;
  }
}
