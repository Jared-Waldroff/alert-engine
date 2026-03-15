package com.pason.alertengine.domain.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pason.alertengine.domain.model.ComparisonOperator;
import com.pason.alertengine.domain.model.SensorReading;
import com.pason.alertengine.domain.model.SensorType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ThresholdExceededConditionTest {

  @ParameterizedTest
  @CsvSource({
      "2999.9, false",
      "3000.0, false",
      "3000.1, true",
      "5000.0, true"
  })
  void evaluate_greaterThan3000_returnsExpected(double value, boolean expected) {
    // Arrange
    var condition = new ThresholdExceededCondition(3000, ComparisonOperator.GREATER_THAN);
    var reading = SensorReading.of("sensor-1", SensorType.PRESSURE, value, "PSI");

    // Act & Assert
    assertThat(condition.evaluate(reading, List.of())).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "200.1, false",
      "200.0, false",
      "199.9, true",
      "0.0, true"
  })
  void evaluate_lessThan200_returnsExpected(double value, boolean expected) {
    // Arrange
    var condition = new ThresholdExceededCondition(200, ComparisonOperator.LESS_THAN);
    var reading = SensorReading.of("sensor-1", SensorType.FLOW_RATE, value, "GPM");

    // Act & Assert
    assertThat(condition.evaluate(reading, List.of())).isEqualTo(expected);
  }

  @Test
  void evaluate_exactlyAtThreshold_returnsFalse() {
    // Arrange
    var condition = new ThresholdExceededCondition(3000, ComparisonOperator.GREATER_THAN);
    var reading = SensorReading.of("sensor-1", SensorType.PRESSURE, 3000.0, "PSI");

    // Act
    boolean result = condition.evaluate(reading, List.of());

    // Assert
    assertThat(result).isFalse();
  }

  @Test
  void constructor_nullOperator_throwsException() {
    // Act & Assert
    assertThatThrownBy(() -> new ThresholdExceededCondition(3000, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void describe_validCondition_returnsReadableDescription() {
    // Arrange
    var condition = new ThresholdExceededCondition(3000, ComparisonOperator.GREATER_THAN);

    // Act
    String description = condition.describe();

    // Assert
    assertThat(description).isEqualTo("Value > 3000.0");
  }

  @Test
  void getType_always_returnsCorrectType() {
    // Arrange
    var condition = new ThresholdExceededCondition(100, ComparisonOperator.GREATER_THAN);

    // Act & Assert
    assertThat(condition.getType()).isEqualTo("THRESHOLD_EXCEEDED");
  }
}
