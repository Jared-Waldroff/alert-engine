package com.pason.alertengine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Application-wide configuration including CORS, bean definitions,
 * and serialization setup for the web dashboard and REST API.
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {

  /**
   * Configures CORS to allow the web dashboard to call the REST API.
   *
   * @param registry the CORS registry to configure
   */
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins("*")
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*");
  }

  /**
   * Customizes the Spring Boot auto-configured {@link ObjectMapper}
   * to register the {@link JavaTimeModule} for JSR-310 date/time support.
   *
   * <p>Uses a customizer instead of replacing the ObjectMapper bean to
   * preserve Spring Boot's default modules and configuration.</p>
   *
   * @return the customizer that registers the JavaTimeModule
   */
  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    return builder -> builder.modules(new JavaTimeModule());
  }
}
