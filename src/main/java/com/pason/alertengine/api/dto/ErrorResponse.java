package com.pason.alertengine.api.dto;

import java.time.Instant;

/**
 * Standardized error response body for all API errors.
 */
public record ErrorResponse(
    String code,
    String message,
    Instant timestamp
) {}
