package com.example.accountservice;

import com.example.accountservice.dto.TransactionRequest;
import com.example.accountservice.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @Test
    public void creditAndDebitShouldComputeBalanceCorrectly() {
        accountService.applyTransaction(new TransactionRequest(
                "acct-test-evt-1",
                "acct-test",
                "CREDIT",
                BigDecimal.valueOf(100),
                "USD",
                Instant.parse("2026-06-07T10:00:00Z")
        ));

        accountService.applyTransaction(new TransactionRequest(
                "acct-test-evt-2",
                "acct-test",
                "DEBIT",
                BigDecimal.valueOf(30),
                "USD",
                Instant.parse("2026-06-07T10:05:00Z")
        ));

        var balance = accountService.getBalance("acct-test");

        assertEquals(0, BigDecimal.valueOf(70).compareTo((BigDecimal) balance.get("balance")));
    }

    @Test
    public void duplicateTransactionShouldNotChangeBalanceTwice() {
        TransactionRequest request = new TransactionRequest(
                "acct-test-evt-duplicate",
                "acct-dup",
                "CREDIT",
                BigDecimal.valueOf(50),
                "USD",
                Instant.parse("2026-06-07T10:00:00Z")
        );

        accountService.applyTransaction(request);
        accountService.applyTransaction(request);

        var balance = accountService.getBalance("acct-dup");

        assertEquals(0, BigDecimal.valueOf(50).compareTo((BigDecimal) balance.get("balance")));
    }
}