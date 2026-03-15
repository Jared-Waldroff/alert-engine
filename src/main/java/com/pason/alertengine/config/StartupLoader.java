package com.pason.alertengine.config;

import com.pason.alertengine.api.service.AlertRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Loads alert rules from the database into the engine on application startup.
 *
 * <p>Runs after Flyway migrations have completed, ensuring the seeded
 * default rules are available in the database before loading.</p>
 */
@Component
public class StartupLoader implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(StartupLoader.class);

  private final AlertRuleService ruleService;

  /**
   * Creates a new startup loader.
   *
   * @param ruleService the service used to reload rules from the database
   */
  public StartupLoader(AlertRuleService ruleService) {
    this.ruleService = ruleService;
  }

  /**
   * Loads all enabled alert rules from the database into the in-memory engine.
   *
   * @param args the application arguments (unused)
   */
  @Override
  public void run(ApplicationArguments args) {
    log.info("Loading alert rules from database into engine...");
    ruleService.reloadEngineRules();
    log.info("Alert engine initialized and ready to process readings.");
  }
}
