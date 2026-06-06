package com.example.eventgateway.service;

import com.example.eventgateway.client.AccountClient;
import com.example.eventgateway.dto.EventRequest;
import com.example.eventgateway.entity.EventRecord;
import com.example.eventgateway.repository.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final AccountClient accountClient;

    public EventService(EventRepository eventRepository,
                        AccountClient accountClient) {
        this.eventRepository = eventRepository;
        this.accountClient = accountClient;
    }

    public EventRecord submitEvent(EventRequest request, String traceId) {
        return eventRepository.findById(request.eventId())
                .orElseGet(() -> createAndApplyEvent(request, traceId));
    }

    private EventRecord createAndApplyEvent(EventRequest request, String traceId) {
        EventRecord event = new EventRecord(
                request.eventId(),
                request.accountId(),
                request.type(),
                request.amount(),
                request.currency(),
                request.eventTimestamp(),
                "RECEIVED"
        );

        eventRepository.save(event);

        try {
            accountClient.applyTransaction(request, traceId);
            event.markApplied();
            return eventRepository.save(event);
        } catch (Exception ex) {
            event.markFailed();
            eventRepository.save(event);

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Account Service is unavailable. Event stored but not applied."
            );
        }
    }

    public EventRecord getEvent(String eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    public List<EventRecord> getEventsForAccount(String accountId) {
        return eventRepository.findByAccountIdOrderByEventTimestampAsc(accountId);
    }
}