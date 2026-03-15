package com.pason.alertengine.domain.dispatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.pason.alertengine.domain.model.Alert;
import com.pason.alertengine.domain.model.AlertSeverity;
import com.pason.alertengine.domain.model.SensorType;
import com.pason.alertengine.persistence.entity.AlertEntity;
import com.pason.alertengine.persistence.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseAlertDispatcherTest {

  @Mock
  private AlertRepository alertRepository;

  @InjectMocks
  private DatabaseAlertDispatcher dispatcher;

  @Test
  void dispatch_validAlert_persistsToRepository() {
    // Arrange
    Alert alert = Alert.builder()
        .ruleName("High Pressure")
        .severity(AlertSeverity.CRITICAL)
        .sensorId("PRESSURE-001")
        .sensorType(SensorType.PRESSURE)
        .triggerValue(3500.0)
        .threshold(3000.0)
        .conditionDescription("Value > 3000.0 PSI")
        .message("Pressure reading of 3500.0 PSI exceeds threshold of 3000.0 PSI")
        .build();

    // Act
    dispatcher.dispatch(alert);

    // Assert
    ArgumentCaptor<AlertEntity> captor = ArgumentCaptor.forClass(AlertEntity.class);
    verify(alertRepository).save(captor.capture());

    AlertEntity saved = captor.getValue();
    assertThat(saved.getAlertId()).isEqualTo(alert.getId());
    assertThat(saved.getRuleName()).isEqualTo("High Pressure");
    assertThat(saved.getSeverity()).isEqualTo(AlertSeverity.CRITICAL);
    assertThat(saved.getSensorId()).isEqualTo("PRESSURE-001");
    assertThat(saved.getSensorType()).isEqualTo(SensorType.PRESSURE);
    assertThat(saved.getTriggerValue()).isEqualTo(3500.0);
    assertThat(saved.getThreshold()).isEqualTo(3000.0);
    assertThat(saved.getConditionDescription()).isEqualTo("Value > 3000.0 PSI");
    assertThat(saved.getMessage())
        .isEqualTo("Pressure reading of 3500.0 PSI exceeds threshold of 3000.0 PSI");
    assertThat(saved.getTimestamp()).isEqualTo(alert.getTimestamp());
  }

  @Test
  void dispatch_repositoryThrowsException_logsErrorAndDoesNotRethrow() {
    // Arrange
    Alert alert = Alert.builder()
        .ruleName("High Pressure")
        .severity(AlertSeverity.CRITICAL)
        .sensorId("PRESSURE-001")
        .sensorType(SensorType.PRESSURE)
        .triggerValue(3500.0)
        .threshold(3000.0)
        .conditionDescription("Value > 3000.0 PSI")
        .message("Pressure reading of 3500.0 PSI exceeds threshold of 3000.0 PSI")
        .build();
    doThrow(new RuntimeException("DB connection failed"))
        .when(alertRepository).save(any(AlertEntity.class));

    // Act & Assert
    assertThatCode(() -> dispatcher.dispatch(alert)).doesNotThrowAnyException();
  }
}
