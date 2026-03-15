package com.pason.alertengine.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pason.alertengine.domain.condition.AlertCondition;
import com.pason.alertengine.domain.condition.OutOfRangeCondition;
import com.pason.alertengine.domain.condition.RateOfChangeCondition;
import com.pason.alertengine.domain.condition.SustainedThresholdCondition;
import com.pason.alertengine.domain.condition.ThresholdExceededCondition;
import com.pason.alertengine.domain.model.AlertRule;
import com.pason.alertengine.domain.model.ComparisonOperator;
import com.pason.alertengine.persistence.entity.AlertRuleEntity;
import java.time.Duration;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Maps between persisted alert rule entities and domain model objects.
 *
 * <p>Handles serialization/deserialization of condition configurations,
 * converting between JSON stored in the database and the appropriate
 * {@link AlertCondition} implementation class.</p>
 */
@Component
public class ConditionConfigMapper {

  private final ObjectMapper objectMapper;

  /**
   * Creates a new condition config mapper.
   *
   * @param objectMapper the Jackson mapper for JSON serialization/deserialization
   */
  public ConditionConfigMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Converts a persisted entity to a domain AlertRule.
   *
   * @param entity the JPA entity
   * @return the domain model AlertRule
   * @throws IllegalArgumentException if the condition type is unknown
   */
  @SuppressWarnings("unchecked")
  public AlertRule toDomain(AlertRuleEntity entity) {
    AlertCondition condition = deserializeCondition(
        entity.getConditionType(), entity.getConditionConfig());

    return AlertRule.builder()
        .id(entity.getId())
        .name(entity.getName())
        .sensorType(entity.getSensorType())
        .condition(condition)
        .severity(entity.getSeverity())
        .enabled(entity.isEnabled())
        .build();
  }

  /**
   * Converts domain condition parameters to a JSON config string.
   *
   * @param condition the domain condition
   * @return JSON string of the condition's configuration
   */
  public String serializeCondition(AlertCondition condition) {
    try {
      return switch (condition.getType()) {
        case ThresholdExceededCondition.TYPE -> {
          ThresholdExceededCondition tc = (ThresholdExceededCondition) condition;
          yield objectMapper.writeValueAsString(Map.of(
              "threshold", tc.getThreshold(),
              "operator", tc.getOperator().name()));
        }
        case RateOfChangeCondition.TYPE -> {
          RateOfChangeCondition rc = (RateOfChangeCondition) condition;
          yield objectMapper.writeValueAsString(Map.of(
              "maxRatePerSecond", rc.getMaxRatePerSecond()));
        }
        case SustainedThresholdCondition.TYPE -> {
          SustainedThresholdCondition sc = (SustainedThresholdCondition) condition;
          yield objectMapper.writeValueAsString(Map.of(
              "threshold", sc.getThreshold(),
              "operator", sc.getOperator().name(),
              "sustainedSeconds", sc.getSustainedDuration().getSeconds()));
        }
        case OutOfRangeCondition.TYPE -> {
          OutOfRangeCondition oc = (OutOfRangeCondition) condition;
          yield objectMapper.writeValueAsString(Map.of(
              "min", oc.getMin(),
              "max", oc.getMax()));
        }
        default -> throw new IllegalArgumentException(
            "Unknown condition type: " + condition.getType());
      };
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize condition config", e);
    }
  }

  @SuppressWarnings("unchecked")
  private AlertCondition deserializeCondition(String conditionType, String configJson) {
    try {
      Map<String, Object> config = objectMapper.readValue(configJson, Map.class);

      return switch (conditionType) {
        case ThresholdExceededCondition.TYPE -> new ThresholdExceededCondition(
            ((Number) config.get("threshold")).doubleValue(),
            ComparisonOperator.valueOf((String) config.get("operator")));

        case RateOfChangeCondition.TYPE -> new RateOfChangeCondition(
            ((Number) config.get("maxRatePerSecond")).doubleValue());

        case SustainedThresholdCondition.TYPE -> new SustainedThresholdCondition(
            ((Number) config.get("threshold")).doubleValue(),
            ComparisonOperator.valueOf((String) config.get("operator")),
            Duration.ofSeconds(((Number) config.get("sustainedSeconds")).longValue()));

        case OutOfRangeCondition.TYPE -> new OutOfRangeCondition(
            ((Number) config.get("min")).doubleValue(),
            ((Number) config.get("max")).doubleValue());

        default -> throw new IllegalArgumentException(
            "Unknown condition type: " + conditionType);
      };
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(
          "Failed to deserialize condition config: " + configJson, e);
    }
  }
}
