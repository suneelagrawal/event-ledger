package com.example.eventgateway.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record EventRequest(
        @NotBlank String eventId,
        @NotBlank String accountId,
        @Pattern(regexp = "CREDIT|DEBIT") String type,
        @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String currency,
        @NotNull Instant eventTimestamp,
        Map<String, Object> metadata
) {
}