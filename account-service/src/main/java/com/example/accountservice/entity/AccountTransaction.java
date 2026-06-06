package com.example.accountservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class AccountTransaction {

    @Id
    private String eventId;

    private String accountId;
    private String type;
    private BigDecimal amount;
    private String currency;
    private Instant eventTimestamp;

    public AccountTransaction() {}

    public AccountTransaction(
            String eventId,
            String accountId,
            String type,
            BigDecimal amount,
            String currency,
            Instant eventTimestamp) {
        this.eventId = eventId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.eventTimestamp = eventTimestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getEventTimestamp() {
        return eventTimestamp;
    }
}