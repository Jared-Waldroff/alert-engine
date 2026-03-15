package com.pason.alertengine.domain.condition;

import static org.assertj.core.api.Assertions.assertThat;

import com.pason.alertengine.domain.model.ComparisonOperator;
import com.pason.alertengine.domain.model.SensorReading;
import com.pason.alertengine.domain.model.SensorType;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SustainedThresholdConditionTest {

  @Test
  void evaluate_sustainedBreach_returnsTrue() {
    // Arrange
    var condition = new SustainedThresholdCondition(
        275, ComparisonOperator.GREATER_THAN, Duration.ofSeconds(10));
    var now = Instant.now();
    var history = List.of(
        new SensorReading("t1", SensorType.TEMPERATURE, 280, "°F", now.minusSeconds(12)),
        new SensorReading("t1", SensorType.TEMPERATURE, 282, "°F", now.minusSeconds(8)),
        new SensorReading("t1", SensorType.TEMPERATURE, 279, "°F", now.minusSeconds(4))
    );
    var current = new SensorReading("t1", SensorType.TEMPERATURE, 281, "°F", now);

    // Act
    boolean result = condition.evaluate(current, history);

    // Assert
    assertThat(result).isTrue();
  }

  @Test
  void evaluate_briefSpike_returnsFalse() {
    // Arrange
    var condition = new SustainedThresholdCondition(
        275, ComparisonOperator.GREATER_THAN, Duration.ofSeconds(10));
    var now = Instant.now();
    // Only 3 seconds of breach — not enough for 10s sustained
    var history = List.of(
        new SensorReading("t1", SensorType.TEMPERATURE, 250, "°F", now.minusSeconds(12)),
        new SensorReading("t1", SensorType.TEMPERATURE, 260, "°F", now.minusSeconds(8)),
        new SensorReading("t1", SensorType.TEMPERATURE, 280, "°F", now.minusSeconds(3))
    );
    var current = new SensorReading("t1", SensorType.TEMPERATURE, 281, "°F", now);

    // Act
    boolean result = condition.evaluate(current, history);

    // Assert — the reading at -8s (260) is below threshold, so not sustained
    assertThat(result).isFalse();
  }

  @Test
  void evaluate_currentBelowThreshold_returnsFalse() {
    // Arrange
    var condition = new SustainedThresholdCondition(
        275, ComparisonOperator.GREATER_THAN, Duration.ofSeconds(10));
    var current = SensorReading.of("t1", SensorType.TEMPERATURE, 270, "°F");

    // Act & Assert
    assertThat(condition.evaluate(current, List.of())).isFalse();
  }

  @Test
  void evaluate_noHistory_returnsFalse() {
    // Arrange
    var condition = new SustainedThresholdCondition(
        275, ComparisonOperator.GREATER_THAN, Duration.ofSeconds(10));
    var current = SensorReading.of("t1", SensorType.TEMPERATURE, 280, "°F");

    // Act & Assert
    assertThat(condition.evaluate(current, List.of())).isFalse();
  }

  @ParameterizedTest
  @CsvSource({
      "2999.9, false",
      "3000.0, false",
      "3000.1, true",
      "5000.0, true"
  })
  void evaluate_sustainedVariousValues_returnsExpected(double value, boolean expected) {
    // Arrange
    var condition = new SustainedThresholdCondition(
        3000, ComparisonOperator.GREATER_THAN, Duration.ofSeconds(10));
    var now = Instant.now();
    var history = List.of(
        new SensorReading("s1", SensorType.PRESSURE, value, "PSI", now.minusSeconds(12)),
        new SensorReading("s1", SensorType.PRESSURE, value, "PSI", now.minusSeconds(8)),
        new SensorReading("s1", SensorType.PRESSURE, value, "PSI", now.minusSeconds(4))
    );
    var current = new SensorReading("s1", SensorType.PRESSURE, value, "PSI", now);

    // Act
    boolean result = condition.evaluate(current, history);

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void describe_validCondition_returnsReadableDescription() {
    // Arrange
    var condition = new SustainedThresholdCondition(
        275, ComparisonOperator.GREATER_THAN, Duration.ofSeconds(30));

    // Act
    String description = condition.describe();

    // Assert
    assertThat(description).isEqualTo("Value > 275.0 for at least 30 seconds");
  }
}
