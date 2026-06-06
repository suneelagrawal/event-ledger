package com.example.accountservice.service;

import com.example.accountservice.dto.TransactionRequest;
import com.example.accountservice.entity.Account;
import com.example.accountservice.entity.AccountTransaction;
import com.example.accountservice.repository.AccountRepository;
import com.example.accountservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Map<String, Object> applyTransaction(TransactionRequest request) {
        if (transactionRepository.existsById(request.eventId())) {
            Account existing = accountRepository.findById(request.accountId())
                    .orElse(new Account(request.accountId()));

            return Map.of(
                    "status", "DUPLICATE",
                    "accountId", existing.getAccountId(),
                    "balance", existing.getBalance()
            );
        }

        Account account = accountRepository.findById(request.accountId())
                .orElse(new Account(request.accountId()));

        if ("CREDIT".equals(request.type())) {
            account.credit(request.amount());
        } else {
            account.debit(request.amount());
        }

        AccountTransaction tx = new AccountTransaction(
                request.eventId(),
                request.accountId(),
                request.type(),
                request.amount(),
                request.currency(),
                request.eventTimestamp()
        );

        transactionRepository.save(tx);
        accountRepository.save(account);

        return Map.of(
                "status", "APPLIED",
                "accountId", account.getAccountId(),
                "balance", account.getBalance()
        );
    }

    public Map<String, Object> getBalance(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElse(new Account(accountId));

        return Map.of(
                "accountId", accountId,
                "balance", account.getBalance()
        );
    }

    public Map<String, Object> getAccount(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElse(new Account(accountId));

        return Map.of(
                "accountId", accountId,
                "balance", account.getBalance(),
                "transactions", transactionRepository.findByAccountIdOrderByEventTimestampDesc(accountId)
        );
    }
}