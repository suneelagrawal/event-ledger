package com.example.eventgateway.controller;

import com.example.eventgateway.client.AccountClient;
import com.example.eventgateway.dto.EventRequest;
import com.example.eventgateway.entity.EventRecord;
import com.example.eventgateway.service.EventService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class EventController {

    private final EventService eventService;
    private final AccountClient accountClient;

    public EventController(EventService eventService,
                           AccountClient accountClient) {
        this.eventService = eventService;
        this.accountClient = accountClient;
    }

    @PostMapping("/events")
    public EventRecord submitEvent(
            @Valid @RequestBody EventRequest request,
            @RequestHeader(value = "X-Trace-Id", required = false) String incomingTraceId) {

        String traceId = incomingTraceId != null ? incomingTraceId : UUID.randomUUID().toString();

        System.out.println("{\"service\":\"event-gateway\",\"traceId\":\"" + traceId + "\",\"eventId\":\"" + request.eventId() + "\",\"message\":\"Received event\"}");

        return eventService.submitEvent(request, traceId);
    }

    @GetMapping("/events/{id}")
    public EventRecord getEvent(@PathVariable String id) {
        return eventService.getEvent(id);
    }

    @GetMapping("/events")
    public List<EventRecord> getEventsByAccount(@RequestParam String account) {
        return eventService.getEventsForAccount(account);
    }

    @GetMapping("/accounts/{accountId}/balance")
    public Map getBalance(
            @PathVariable String accountId,
            @RequestHeader(value = "X-Trace-Id", required = false) String incomingTraceId) {

        String traceId = incomingTraceId != null ? incomingTraceId : UUID.randomUUID().toString();
        return accountClient.getBalance(accountId, traceId);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "event-gateway");
    }
}