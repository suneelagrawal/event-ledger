package com.example.accountservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionRequest(
        @NotBlank String eventId,
        @NotBlank String accountId,
        @Pattern(regexp = "CREDIT|DEBIT") String type,
        @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String currency,
        @NotNull Instant eventTimestamp
) {
}