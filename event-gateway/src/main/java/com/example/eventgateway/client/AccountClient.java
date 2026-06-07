package com.example.eventgateway.client;

import com.example.eventgateway.dto.EventRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AccountClient {

    private final RestClient restClient;
    private final String baseUrl;

    public AccountClient(RestClient restClient,
                         @Value("${account-service.base-url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "accountServiceFallback")
    public Map applyTransaction(EventRequest request, String traceId) {
        return restClient.post()
                .uri(baseUrl + "/accounts/" + request.accountId() + "/transactions")
                .header("X-Trace-Id", traceId)
                .body(request)
                .retrieve()
                .body(Map.class);
    }

    public Map accountServiceFallback(EventRequest request, String traceId, Throwable ex) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Account Service is currently unavailable. Please retry later."
        );
    }

    public Map getBalance(String accountId, String traceId) {
        return restClient.get()
                .uri(baseUrl + "/accounts/" + accountId + "/balance")
                .header("X-Trace-Id", traceId)
                .retrieve()
                .body(Map.class);
    }
}