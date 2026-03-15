package com.pason.alertengine.domain.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pason.alertengine.domain.model.SensorReading;
import com.pason.alertengine.domain.model.SensorType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RateOfChangeConditionTest {

  @Test
  void evaluate_noHistory_returnsFalse() {
    // Arrange
    var condition = new RateOfChangeCondition(100);
    var reading = SensorReading.of("s1", SensorType.PRESSURE, 3000, "PSI");

    // Act & Assert
    assertThat(condition.evaluate(reading, List.of())).isFalse();
  }

  @Test
  void evaluate_rapidIncrease_returnsTrue() {
    // Arrange
    var condition = new RateOfChangeCondition(50); // max 50 PSI/sec
    var now = Instant.now();
    var previous = new SensorReading("s1", SensorType.PRESSURE, 2500, "PSI", now.minusSeconds(1));
    var current = new SensorReading("s1", SensorType.PRESSURE, 2600, "PSI", now);

    // Act
    boolean result = condition.evaluate(current, List.of(previous));

    // Assert — 100 PSI/sec > 50 PSI/sec threshold
    assertThat(result).isTrue();
  }

  @Test
  void evaluate_slowChange_returnsFalse() {
    // Arrange
    var condition = new RateOfChangeCondition(50);
    var now = Instant.now();
    var previous = new SensorReading("s1", SensorType.PRESSURE, 2500, "PSI", now.minusSeconds(1));
    var current = new SensorReading("s1", SensorType.PRESSURE, 2520, "PSI", now);

    // Act
    boolean result = condition.evaluate(current, List.of(previous));

    // Assert — 20 PSI/sec < 50 PSI/sec threshold
    assertThat(result).isFalse();
  }

  @Test
  void evaluate_rapidDecrease_returnsTrue() {
    // Arrange
    var condition = new RateOfChangeCondition(50);
    var now = Instant.now();
    var previous = new SensorReading("s1", SensorType.PRESSURE, 2600, "PSI", now.minusSeconds(1));
    var current = new SensorReading("s1", SensorType.PRESSURE, 2500, "PSI", now);

    // Act
    boolean result = condition.evaluate(current, List.of(previous));

    // Assert
    assertThat(result).isTrue();
  }

  @ParameterizedTest
  @CsvSource({
      "49.9, false",
      "50.0, false",
      "50.1, true",
      "200.0, true"
  })
  void evaluate_variousRates_returnsExpected(double ratePerSecond, boolean expected) {
    // Arrange
    var condition = new RateOfChangeCondition(50);
    var now = Instant.now();
    var previous = new SensorReading("s1", SensorType.PRESSURE, 2500, "PSI", now.minusSeconds(1));
    var current = new SensorReading("s1", SensorType.PRESSURE, 2500 + ratePerSecond, "PSI", now);

    // Act
    boolean result = condition.evaluate(current, List.of(previous));

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void constructor_negativeRate_throwsException() {
    // Act & Assert
    assertThatThrownBy(() -> new RateOfChangeCondition(-1))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
