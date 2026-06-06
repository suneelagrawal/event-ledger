package com.example.eventgateway.repository;

import com.example.eventgateway.entity.EventRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<EventRecord, String> {
    List<EventRecord> findByAccountIdOrderByEventTimestampAsc(String accountId);
}