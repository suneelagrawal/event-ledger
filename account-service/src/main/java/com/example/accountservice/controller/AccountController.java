package com.example.accountservice.controller;

import com.example.accountservice.dto.TransactionRequest;
import com.example.accountservice.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/accounts/{accountId}/transactions")
    public Map<String, Object> applyTransaction(
            @PathVariable String accountId,
            @Valid @RequestBody TransactionRequest request,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {

        JsonLogger.info(traceId, "Applying transaction " + request.getEventId());
        return accountService.applyTransaction(request);
    }

    @GetMapping("/accounts/{accountId}/balance")
    public Map<String, Object> getBalance(@PathVariable String accountId) {
        return accountService.getBalance(accountId);
    }

    @GetMapping("/accounts/{accountId}")
    public Map<String, Object> getAccount(@PathVariable String accountId) {
        return accountService.getAccount(accountId);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "account-service");
    }

}