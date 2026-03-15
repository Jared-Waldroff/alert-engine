package com.pason.alertengine.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pason.alertengine.api.dto.AlertRuleResponse;
import com.pason.alertengine.api.dto.CreateAlertRuleRequest;
import com.pason.alertengine.api.exception.ResourceNotFoundException;
import com.pason.alertengine.domain.condition.OutOfRangeCondition;
import com.pason.alertengine.domain.condition.RateOfChangeCondition;
import com.pason.alertengine.domain.condition.SustainedThresholdCondition;
import com.pason.alertengine.domain.condition.ThresholdExceededCondition;
import com.pason.alertengine.domain.model.AlertRule;
import com.pason.alertengine.engine.AlertEngine;
import com.pason.alertengine.persistence.ConditionConfigMapper;
import com.pason.alertengine.persistence.entity.AlertRuleEntity;
import com.pason.alertengine.persistence.repository.AlertRuleRepository;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for managing alert rules — bridges between the REST API and the domain.
 *
 * <p>Handles CRUD operations on alert rules, persists them to the database,
 * and keeps the in-memory alert engine in sync with the persisted state.</p>
 */
@Service
public class AlertRuleService {

  private static final Logger log = LoggerFactory.getLogger(AlertRuleService.class);

  private final AlertRuleRepository ruleRepository;
  private final ConditionConfigMapper conditionMapper;
  private final AlertEngine alertEngine;
  private final ObjectMapper objectMapper;

  /**
   * Creates a new alert rule service.
   *
   * @param ruleRepository  the repository for persisting alert rules
   * @param conditionMapper the mapper for converting between entities and domain objects
   * @param alertEngine     the engine to register rules with for evaluation
   * @param objectMapper    the Jackson mapper for serializing condition configs
   */
  public AlertRuleService(AlertRuleRepository ruleRepository,
      ConditionConfigMapper conditionMapper,
      AlertEngine alertEngine,
      ObjectMapper objectMapper) {
    this.ruleRepository = ruleRepository;
    this.conditionMapper = conditionMapper;
    this.alertEngine = alertEngine;
    this.objectMapper = objectMapper;
  }

  /**
   * Returns all alert rules as response DTOs.
   *
   * @return list of all alert rules, both enabled and disabled
   */
  public List<AlertRuleResponse> getAllRules() {
    return ruleRepository.findAll().stream()
        .map(this::toResponse)
        .toList();
  }

  /**
   * Returns a single rule by ID, or throws if not found.
   *
   * @param id the rule's database ID
   * @return the matching alert rule
   * @throws com.pason.alertengine.api.exception.ResourceNotFoundException if no rule exists
   */
  public AlertRuleResponse getRuleById(Long id) {
    AlertRuleEntity entity = ruleRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Alert rule not found with ID: " + id));
    return toResponse(entity);
  }

  /**
   * Creates a new alert rule, persists it, and registers it with the engine.
   *
   * @param request the rule configuration including condition type and parameters
   * @return the created rule with its generated ID
   * @throws IllegalArgumentException if the condition configuration is invalid
   */
  public AlertRuleResponse createRule(CreateAlertRuleRequest request) {
    // Validate conditionType before persisting to prevent poisoning the database
    Set<String> validTypes = Set.of(
        ThresholdExceededCondition.TYPE, RateOfChangeCondition.TYPE,
        SustainedThresholdCondition.TYPE, OutOfRangeCondition.TYPE);
    if (!validTypes.contains(request.conditionType())) {
      throw new IllegalArgumentException(
          "Unknown condition type: " + request.conditionType()
              + ". Valid types: " + validTypes);
    }

    AlertRuleEntity entity = new AlertRuleEntity();
    entity.setName(request.name());
    entity.setSensorType(request.sensorType());
    entity.setConditionType(request.conditionType());
    entity.setSeverity(request.severity());
    entity.setEnabled(true);

    try {
      String configJson = objectMapper.writeValueAsString(request.conditionConfig());
      entity.setConditionConfig(configJson);
      // Validate the config can be deserialized before saving
      conditionMapper.toDomain(entity);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Invalid condition configuration", e);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Invalid condition configuration for type " + request.conditionType()
              + ": " + e.getMessage(), e);
    }

    entity = ruleRepository.save(entity);
    log.info("Created alert rule: id={}, name={}", entity.getId(), entity.getName());

    // Register with the engine immediately
    AlertRule domainRule = conditionMapper.toDomain(entity);
    alertEngine.addRule(domainRule);

    return toResponse(entity);
  }

  /**
   * Toggles the enabled status of a rule.
   *
   * @param id the rule's database ID
   * @return the updated rule with its new enabled status
   * @throws com.pason.alertengine.api.exception.ResourceNotFoundException if no rule exists
   */
  public AlertRuleResponse toggleRule(Long id) {
    AlertRuleEntity entity = ruleRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Alert rule not found with ID: " + id));

    entity.setEnabled(!entity.isEnabled());
    entity = ruleRepository.save(entity);
    log.info("Toggled rule: id={}, enabled={}", entity.getId(), entity.isEnabled());

    // Reload all rules in the engine to sync
    reloadEngineRules();

    return toResponse(entity);
  }

  /**
   * Deletes a rule by ID.
   *
   * @param id the rule's database ID
   * @throws com.pason.alertengine.api.exception.ResourceNotFoundException if no rule exists
   */
  public void deleteRule(Long id) {
    if (!ruleRepository.existsById(id)) {
      throw new ResourceNotFoundException("Alert rule not found with ID: " + id);
    }
    ruleRepository.deleteById(id);
    alertEngine.removeRule(id);
    log.info("Deleted alert rule: id={}", id);
  }

  /**
   * Loads all enabled rules from the database into the engine.
   * Called on application startup and after rule modifications.
   */
  public void reloadEngineRules() {
    List<AlertRule> rules = ruleRepository.findByEnabledTrue().stream()
        .map(conditionMapper::toDomain)
        .toList();
    alertEngine.loadRules(rules);
  }

  private AlertRuleResponse toResponse(AlertRuleEntity entity) {
    AlertRule domain = conditionMapper.toDomain(entity);
    return new AlertRuleResponse(
        entity.getId(),
        entity.getName(),
        entity.getSensorType(),
        entity.getConditionType(),
        domain.getCondition().describe(),
        entity.getSeverity(),
        entity.isEnabled(),
        entity.getCreatedAt());
  }
}
