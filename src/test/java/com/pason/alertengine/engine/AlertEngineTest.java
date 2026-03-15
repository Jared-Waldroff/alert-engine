package com.pason.alertengine.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.pason.alertengine.domain.condition.OutOfRangeCondition;
import com.pason.alertengine.domain.condition.ThresholdExceededCondition;
import com.pason.alertengine.domain.dispatch.AlertDispatcher;
import com.pason.alertengine.domain.model.Alert;
import com.pason.alertengine.domain.model.AlertRule;
import com.pason.alertengine.domain.model.AlertSeverity;
import com.pason.alertengine.domain.model.ComparisonOperator;
import com.pason.alertengine.domain.model.SensorReading;
import com.pason.alertengine.domain.model.SensorType;
import java.util.List;
import org.junit.jupiter.api.Test;

class AlertEngineTest {

  private final AlertRule pressureRule = AlertRule.builder()
      .id(1L)
      .name("High Pressure")
      .sensorType(SensorType.PRESSURE)
      .condition(new ThresholdExceededCondition(3000, ComparisonOperator.GREATER_THAN))
      .severity(AlertSeverity.CRITICAL)
      .build();

  private final AlertRule flowRule = AlertRule.builder()
      .id(2L)
      .name("Flow Out of Range")
      .sensorType(SensorType.FLOW_RATE)
      .condition(new OutOfRangeCondition(200, 700))
      .severity(AlertSeverity.WARNING)
      .build();

  @Test
  void processReading_ruleTriggered_returnsAlert() {
    // Arrange
    var engine = new AlertEngine(List.of(pressureRule), List.of());
    var reading = SensorReading.of("s1", SensorType.PRESSURE, 3500, "PSI");

    // Act
    List<Alert> alerts = engine.processReading(reading);

    // Assert
    assertThat(alerts).hasSize(1);
    assertThat(alerts.get(0).getRuleName()).isEqualTo("High Pressure");
    assertThat(alerts.get(0).getSeverity()).isEqualTo(AlertSeverity.CRITICAL);
  }

  @Test
  void processReading_ruleNotTriggered_returnsEmpty() {
    // Arrange
    var engine = new AlertEngine(List.of(pressureRule), List.of());
    var reading = SensorReading.of("s1", SensorType.PRESSURE, 2500, "PSI");

    // Act & Assert
    assertThat(engine.processReading(reading)).isEmpty();
  }

  @Test
  void processReading_wrongSensorType_skipsRule() {
    // Arrange
    var engine = new AlertEngine(List.of(pressureRule), List.of());
    var reading = SensorReading.of("t1", SensorType.TEMPERATURE, 9999, "°F");

    // Act & Assert
    assertThat(engine.processReading(reading)).isEmpty();
  }

  @Test
  void processReading_disabledRule_skipsRule() {
    // Arrange
    var disabledRule = AlertRule.builder()
        .id(3L).name("Disabled").sensorType(SensorType.PRESSURE)
        .condition(new ThresholdExceededCondition(0, ComparisonOperator.GREATER_THAN))
        .severity(AlertSeverity.INFO).enabled(false).build();
    var engine = new AlertEngine(List.of(disabledRule), List.of());
    var reading = SensorReading.of("s1", SensorType.PRESSURE, 3500, "PSI");

    // Act & Assert
    assertThat(engine.processReading(reading)).isEmpty();
  }

  @Test
  void processReading_multipleRulesMatch_returnsMultipleAlerts() {
    // Arrange
    var secondRule = AlertRule.builder()
        .id(4L).name("Also High").sensorType(SensorType.PRESSURE)
        .condition(new ThresholdExceededCondition(2000, ComparisonOperator.GREATER_THAN))
        .severity(AlertSeverity.WARNING).build();
    var engine = new AlertEngine(List.of(pressureRule, secondRule), List.of());
    var reading = SensorReading.of("s1", SensorType.PRESSURE, 3500, "PSI");

    // Act & Assert
    assertThat(engine.processReading(reading)).hasSize(2);
  }

  @Test
  void processReading_ruleTriggered_callsAllDispatchers() {
    // Arrange
    var mockDispatcher1 = mock(AlertDispatcher.class);
    var mockDispatcher2 = mock(AlertDispatcher.class);
    var engine = new AlertEngine(List.of(pressureRule), List.of(mockDispatcher1, mockDispatcher2));

    // Act
    engine.processReading(SensorReading.of("s1", SensorType.PRESSURE, 3500, "PSI"));

    // Assert
    verify(mockDispatcher1, times(1)).dispatch(any(Alert.class));
    verify(mockDispatcher2, times(1)).dispatch(any(Alert.class));
  }

  @Test
  void processReading_noTrigger_doesNotCallDispatchers() {
    // Arrange
    var mockDispatcher = mock(AlertDispatcher.class);
    var engine = new AlertEngine(List.of(pressureRule), List.of(mockDispatcher));

    // Act
    engine.processReading(SensorReading.of("s1", SensorType.PRESSURE, 2500, "PSI"));

    // Assert
    verify(mockDispatcher, never()).dispatch(any());
  }

  @Test
  void processReading_multipleReadings_incrementsMetrics() {
    // Arrange
    var engine = new AlertEngine(List.of(pressureRule), List.of());

    // Act
    engine.processReading(SensorReading.of("s1", SensorType.PRESSURE, 3500, "PSI"));
    engine.processReading(SensorReading.of("s1", SensorType.PRESSURE, 2500, "PSI"));

    // Assert
    assertThat(engine.getReadingsProcessed()).isEqualTo(2);
    assertThat(engine.getAlertsTriggered()).isEqualTo(1);
  }
}
