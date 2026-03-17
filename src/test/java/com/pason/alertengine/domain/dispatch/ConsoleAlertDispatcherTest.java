package com.pason.alertengine.domain.dispatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.pason.alertengine.domain.model.Alert;
import com.pason.alertengine.domain.model.AlertSeverity;
import com.pason.alertengine.domain.model.SensorType;
import org.junit.jupiter.api.Test;

class ConsoleAlertDispatcherTest {

  private final ConsoleAlertDispatcher dispatcher = new ConsoleAlertDispatcher();

  @Test
  void dispatch_validAlert_doesNotThrow() {
    // Arrange
    Alert alert = Alert.builder()
        .ruleName("Test Rule")
        .severity(AlertSeverity.CRITICAL)
        .sensorId("s1")
        .sensorType(SensorType.PRESSURE)
        .triggerValue(3500)
        .threshold(3000)
        .conditionDescription("Value > 3000.0")
        .message("Test alert")
        .build();

    // Act & Assert
    assertThatCode(() -> dispatcher.dispatch(alert)).doesNotThrowAnyException();
  }

  @Test
  void getName_always_returnsConsole() {
    // Act & Assert
    assertThat(dispatcher.getName()).isEqualTo("Console");
  }
}
