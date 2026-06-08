# Event Ledger

## Overview

Event Ledger is a distributed financial transaction processing system implemented using two independent Spring Boot microservices.

* **Event Gateway** – Public-facing API that validates requests, enforces idempotency, stores events, and forwards transactions.
* **Account Service** – Internal service responsible for account balances and transaction history.

---

# Architecture

```text
                    +----------------------+
                    |        Client        |
                    +----------+-----------+
                               |
                        POST /events
                               |
                               v
                    +----------------------+
                    |    Event Gateway     |
                    |----------------------|
                    | Validation           |
                    | Idempotency          |
                    | Event Store          |
                    +----------+-----------+
                               |
                               | REST + X-Trace-Id
                               |
                               v
                    +----------------------+
                    |   Account Service    |
                    |----------------------|
                    | Transactions         |
                    | Balance              |
                    | Account Store        |
                    +----------------------+
```

Both services run independently and maintain separate H2 databases.

---

# Services

| Service         | Port | Responsibility                              |
| --------------- | ---- | ------------------------------------------- |
| Event Gateway   | 8080 | Accepts events and exposes public APIs      |
| Account Service | 8081 | Applies transactions and maintains balances |

---

# Implemented Features

* ✅ Idempotent event processing
* ✅ Out-of-order event handling
* ✅ Balance computation
* ✅ Request validation
* ✅ Separate microservices and databases
* ✅ Graceful degradation when the Account Service is unavailable
* ✅ Health endpoints
* ✅ Trace ID propagation
* ✅ REST-based service communication
* ✅ Circuit breaker
* ✅ Automated test cases including end to end integration test

---

# Quick Start

Start Account Service:

```bash
cd account-service
mvn spring-boot:run
```

Start Event Gateway:

```bash
cd event-gateway
mvn spring-boot:run
```

Health checks:

```bash
curl http://localhost:8081/health
curl http://localhost:8080/health
```

---

# Public APIs

## Event Gateway

| Method | Endpoint                 |
| ------ | ------------------------ |
| POST   | `/events`                |
| GET    | `/events/{id}`           |
| GET    | `/events?account={id}`   |
| GET    | `/accounts/{id}/balance` |
| GET    | `/health`                |

## Account Service

| Method | Endpoint                      |
| ------ | ----------------------------- |
| POST   | `/accounts/{id}/transactions` |
| GET    | `/accounts/{id}`              |
| GET    | `/accounts/{id}/balance`      |
| GET    | `/health`                     |

---

# Internal API Contract

## Event Gateway → Account Service

### Endpoint

`POST /accounts/{accountId}/transactions`

### Responsibilities

**Event Gateway**

* Validate request
* Enforce idempotency
* Persist event
* Generate and propagate trace ID

**Account Service**

* Apply transaction
* Compute balance
* Persist transaction
* Maintain account state

### Request Headers

| Header       | Description                            |
| ------------ | -------------------------------------- |
| Content-Type | application/json                       |
| X-Trace-Id   | Correlation ID for distributed tracing |

### Request

```json
{
  "eventId": "evt-001",
  "accountId": "acct-123",
  "type": "CREDIT",
  "amount": 150.00,
  "currency": "USD",
  "eventTimestamp": "2026-05-15T14:02:11Z"
}
```

### Validation Rules

* `eventId` must be present and unique
* `accountId` is required
* `type` must be `CREDIT` or `DEBIT`
* `amount` must be greater than zero
* `currency` is required
* `eventTimestamp` must be a valid ISO-8601 timestamp

### Success Response

```json
{
  "status": "APPLIED",
  "accountId": "acct-123",
  "balance": 150.00
}
```

### Duplicate Response

```json
{
  "status": "DUPLICATE",
  "accountId": "acct-123",
  "balance": 150.00
}
```

### Error Responses

| Status | Description            |
| ------ | ---------------------- |
| 400    | Validation failure     |
| 503    | Dependency unavailable |
| 500    | Internal server error  |

---

# Design Decisions

## Idempotency

Duplicate `eventId` values are detected by the Event Gateway. Duplicate events are not forwarded to the Account Service.

## Out-of-Order Events

Events are stored with their original `eventTimestamp` and retrieved in chronological order.

## Balance Computation

```
Net Balance = Σ(CREDIT) − Σ(DEBIT)
```

The final balance is independent of arrival order.

## Service Isolation

The Gateway and Account Service have separate codebases and separate databases, communicating only through REST APIs.

---

## Custom Metrics

The Event Gateway exposes custom Micrometer metrics through Spring Boot Actuator.

| Metric | Description |
|---|---|
| `events.submitted.total` | Total number of event submissions received |
| `events.duplicate.total` | Total number of duplicate event submissions |
| `events.failed.total` | Total number of events that failed while calling Account Service |

Validate:

```bash
curl http://localhost:8080/actuator/metrics/events.submitted.total
curl http://localhost:8080/actuator/metrics/events.duplicate.total
curl http://localhost:8080/actuator/metrics/events.failed.total

## Automated Tests

Tests can be run using Maven:

```bash
cd account-service
mvn test

cd ../event-gateway
mvn test

# Future Improvements

* OpenTelemetry tracing
* Prometheus/Grafana metrics
* PostgreSQL instead of H2
* OpenAPI (Swagger) documentation
* Asynchronous retry mechanism for failed downstream calls
