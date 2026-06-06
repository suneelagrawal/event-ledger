package com.example.eventgateway.client;

import com.example.eventgateway.dto.EventRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class AccountClient {

    private final RestClient restClient;
    private final String baseUrl;

    public AccountClient(RestClient restClient,
                         @Value("${account-service.base-url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    public Map applyTransaction(EventRequest request, String traceId) {
        return restClient.post()
                .uri(baseUrl + "/accounts/" + request.accountId() + "/transactions")
                .header("X-Trace-Id", traceId)
                .body(request)
                .retrieve()
                .body(Map.class);
    }

    public Map getBalance(String accountId, String traceId) {
        return restClient.get()
                .uri(baseUrl + "/accounts/" + accountId + "/balance")
                .header("X-Trace-Id", traceId)
                .retrieve()
                .body(Map.class);
    }
}