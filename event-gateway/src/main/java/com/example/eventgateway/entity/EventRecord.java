package com.example.eventgateway.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class EventRecord {

    @Id
    private String eventId;

    private String accountId;
    private String type;
    private BigDecimal amount;
    private String currency;
    private Instant eventTimestamp;
    private String status;

    public EventRecord() {}

    public EventRecord(String eventId, String accountId, String type,
                       BigDecimal amount, String currency,
                       Instant eventTimestamp, String status) {
        this.eventId = eventId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.eventTimestamp = eventTimestamp;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void markApplied() {
        this.status = "APPLIED";
    }

    public void markFailed() {
        this.status = "FAILED";
    }
}