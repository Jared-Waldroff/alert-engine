package com.pason.alertengine.api.controller;

import com.pason.alertengine.api.dto.AlertRuleResponse;
import com.pason.alertengine.api.dto.CreateAlertRuleRequest;
import com.pason.alertengine.api.service.AlertRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for managing alert rules.
 *
 * <p>Alert rules define the conditions under which the engine triggers
 * an alert. Each rule specifies a sensor type, a condition with parameters,
 * and a severity level.</p>
 *
 * <p>Rules can be enabled or disabled without deletion. Disabled rules
 * are not evaluated by the engine but are preserved for future re-activation.</p>
 */
@RestController
@RequestMapping("/api/rules")
@Tag(name = "Alert Rules", description = "CRUD operations for configurable alert rules")
public class AlertRuleController {

  private final AlertRuleService ruleService;

  /**
   * Creates a new alert rule controller.
   *
   * @param ruleService the service handling alert rule CRUD operations
   */
  public AlertRuleController(AlertRuleService ruleService) {
    this.ruleService = ruleService;
  }

  /**
   * Returns all alert rules, both enabled and disabled.
   *
   * @return list of alert rules with their current status
   */
  @GetMapping
  @Operation(summary = "List all alert rules",
      description = "Returns all rules including disabled ones.")
  public List<AlertRuleResponse> getAllRules() {
    return ruleService.getAllRules();
  }

  /**
   * Returns a single alert rule by ID.
   *
   * @param id the rule ID
   * @return the alert rule
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get alert rule by ID",
      description = "Returns a single alert rule by its unique identifier, including its "
          + "condition type, parameters, severity, and current enabled/disabled status.")
  public AlertRuleResponse getRule(@PathVariable Long id) {
    return ruleService.getRuleById(id);
  }

  /**
   * Creates a new alert rule and immediately registers it with the engine.
   *
   * @param request the rule configuration including condition type and parameters
   * @return the created rule with its generated ID
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a new alert rule",
      description = "Creates an alert rule from the given configuration and immediately "
          + "registers it with the engine. The response includes the generated rule ID. "
          + "The rule is enabled by default and will be evaluated against incoming readings.")
  public AlertRuleResponse createRule(@Valid @RequestBody CreateAlertRuleRequest request) {
    return ruleService.createRule(request);
  }

  /**
   * Toggles the enabled/disabled state of a rule.
   *
   * @param id the rule ID
   * @return the updated rule
   */
  @PatchMapping("/{id}/toggle")
  @Operation(summary = "Toggle rule enabled/disabled",
      description = "Flips the enabled state of the specified rule. A disabled rule is "
          + "skipped during reading evaluation but remains in the database for re-activation. "
          + "Returns the updated rule with its new enabled status.")
  public AlertRuleResponse toggleRule(@PathVariable Long id) {
    return ruleService.toggleRule(id);
  }

  /**
   * Deletes a rule permanently.
   *
   * @param id the rule ID
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete an alert rule",
      description = "Permanently removes the rule from the database and unregisters it "
          + "from the engine. Any alerts previously triggered by this rule are preserved "
          + "in the alert history. Returns 204 No Content on success.")
  public void deleteRule(@PathVariable Long id) {
    ruleService.deleteRule(id);
  }
}
