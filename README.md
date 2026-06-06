# Event Ledger

## Overview
Event Ledger is a two-service Spring Boot system for processing financial transaction events.

It contains:
- Event Gateway API: public-facing service that accepts events, validates them, enforces idempotency, stores event records, and calls Account Service.
- Account Service: internal service that manages account balances and transaction history.

## Architecture

Client -> Event Gateway API -> Account Service

Each service runs independently and uses its own H2 in-memory database. The services communicate using synchronous REST calls.

## Services

| Service | Port | Responsibility |
|---|---:|---|
| Event Gateway | 8080 | Accepts transaction events and exposes event APIs |
| Account Service | 8081 | Applies transactions and manages balances |

## API Contract: Gateway to Account Service

### POST /accounts/{accountId}/transactions

Request header:

| Header | Description |
|---|---|
| X-Trace-Id | Trace/correlation ID propagated from Gateway |

Request body:

```json
{
  "eventId": "evt-001",
  "accountId": "acct-123",
  "type": "CREDIT",
  "amount": 150.00,
  "currency": "USD",
  "eventTimestamp": "2026-05-15T14:02:11Z"
}