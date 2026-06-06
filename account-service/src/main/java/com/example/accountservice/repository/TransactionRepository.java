package com.example.accountservice.repository;

import com.example.accountservice.entity.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<AccountTransaction, String> {
    List<AccountTransaction> findByAccountIdOrderByEventTimestampDesc(String accountId);
}