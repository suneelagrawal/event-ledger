package com.example.eventgateway;

import com.example.eventgateway.client.AccountClient;
import com.example.eventgateway.dto.EventRequest;
import com.example.eventgateway.repository.EventRepository;
import com.example.eventgateway.service.EventService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;


public class EventServiceTests {

    @Test
    public void duplicateEventShouldNotCallAccountServiceTwice() {
        EventRepository eventRepository = mock(EventRepository.class);
        AccountClient accountClient = mock(AccountClient.class);

        EventService eventService =
                new EventService(
                        eventRepository,
                        accountClient,
                        new SimpleMeterRegistry()
                );

        EventRequest request = new EventRequest(
                "evt-test-001",
                "acct-test",
                "CREDIT",
                BigDecimal.valueOf(100),
                "USD",
                Instant.parse("2026-06-07T10:00:00Z"),
                null
        );

        when(eventRepository.findById("evt-test-001"))
                .thenReturn(java.util.Optional.empty())
                .thenReturn(java.util.Optional.of(
                        new com.example.eventgateway.entity.EventRecord(
                                "evt-test-001",
                                "acct-test",
                                "CREDIT",
                                BigDecimal.valueOf(100),
                                "USD",
                                Instant.parse("2026-06-07T10:00:00Z"),
                                "APPLIED"
                        )
                ));

        when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountClient.applyTransaction(any(), any())).thenReturn(Map.of("status", "APPLIED"));

        eventService.submitEvent(request, "trace-test");
        eventService.submitEvent(request, "trace-test");

        verify(accountClient, times(1)).applyTransaction(any(), any());
    }

    @Test
    public void accountServiceFailureShouldReturnServiceUnavailable() {
        EventRepository eventRepository = mock(EventRepository.class);
        AccountClient accountClient = mock(AccountClient.class);

        EventService eventService =
                new EventService(
                        eventRepository,
                        accountClient,
                        new SimpleMeterRegistry()
                );

        EventRequest request = new EventRequest(
                "evt-fail-001",
                "acct-fail",
                "CREDIT",
                BigDecimal.valueOf(100),
                "USD",
                Instant.parse("2026-06-07T10:00:00Z"),
                null
        );

        when(eventRepository.findById("evt-fail-001")).thenReturn(java.util.Optional.empty());
        when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountClient.applyTransaction(any(), any()))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                        "Account Service is unavailable"
                ));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> eventService.submitEvent(request, "trace-test")
        );

        assertEquals(503, ex.getStatusCode().value());
    }

    @Test
    public void submittedMetricShouldIncrement() {

        EventRepository eventRepository = mock(EventRepository.class);
        AccountClient accountClient = mock(AccountClient.class);

        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        EventService eventService =
                new EventService(
                        eventRepository,
                        accountClient,
                        registry
                );

        EventRequest request = new EventRequest(
                "evt-metric-001",
                "acct-metric",
                "CREDIT",
                BigDecimal.valueOf(100),
                "USD",
                Instant.parse("2026-06-07T10:00:00Z"),
                null
        );

        when(eventRepository.findById(any())).thenReturn(Optional.empty());
        when(eventRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountClient.applyTransaction(any(), any()))
                .thenReturn(Map.of("status", "APPLIED"));

        eventService.submitEvent(request, "trace-001");

        assertEquals(
                1.0,
                registry.get("events.submitted.total")
                        .counter()
                        .count()
        );
    }    

    @Test
    void traceIdShouldBePassedToAccountClient() {
        EventRepository eventRepository = mock(EventRepository.class);
        AccountClient accountClient = mock(AccountClient.class);

        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        EventService eventService =
                new EventService(
                        eventRepository,
                        accountClient,
                        registry
                );

        EventRequest request = new EventRequest(
                "evt-trace-test",
                "acct-trace-test",
                "CREDIT",
                BigDecimal.valueOf(25),
                "USD",
                Instant.parse("2026-06-07T10:00:00Z"),
                null
        );

        when(eventRepository.findById("evt-trace-test")).thenReturn(java.util.Optional.empty());
        when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountClient.applyTransaction(any(), any())).thenReturn(Map.of("status", "APPLIED"));

        eventService.submitEvent(request, "trace-unit-test-001");

        verify(accountClient).applyTransaction(any(), eq("trace-unit-test-001"));
    }
    
}