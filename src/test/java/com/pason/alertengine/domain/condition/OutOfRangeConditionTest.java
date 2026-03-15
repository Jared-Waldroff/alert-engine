package com.pason.alertengine.domain.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pason.alertengine.domain.model.SensorReading;
import com.pason.alertengine.domain.model.SensorType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OutOfRangeConditionTest {

  @ParameterizedTest
  @CsvSource({
      "199.9, true",
      "200.0, false",
      "450.0, false",
      "700.0, false",
      "700.1, true"
  })
  void evaluate_range200to700_returnsExpected(double value, boolean expected) {
    // Arrange
    var condition = new OutOfRangeCondition(200, 700);
    var reading = SensorReading.of("f1", SensorType.FLOW_RATE, value, "GPM");

    // Act & Assert
    assertThat(condition.evaluate(reading, List.of())).isEqualTo(expected);
  }

  @Test
  void constructor_minGreaterThanMax_throwsException() {
    // Act & Assert
    assertThatThrownBy(() -> new OutOfRangeCondition(700, 200))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void constructor_minEqualsMax_throwsException() {
    // Act & Assert
    assertThatThrownBy(() -> new OutOfRangeCondition(500, 500))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void describe_validCondition_returnsReadableDescription() {
    // Arrange
    var condition = new OutOfRangeCondition(200, 700);

    // Act & Assert
    assertThat(condition.describe()).isEqualTo("Value outside range [200.0, 700.0]");
  }
}
