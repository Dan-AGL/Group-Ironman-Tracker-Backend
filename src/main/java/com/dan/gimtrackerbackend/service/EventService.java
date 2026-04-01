package com.dan.gimtrackerbackend.service;

import com.dan.gimtrackerbackend.dto.CreateEventRequest;
import com.dan.gimtrackerbackend.model.EventEntity;
import com.dan.gimtrackerbackend.repository.EventRepository;
import org.springframework.stereotype.Service;

/**
 * Contains the business logic for working with events.
 * Right now the logic is simple: take a validated request, map it into
 * a JPA entity, and persist it. As the project grows, rules around
 * event types, group membership, or payload validation belong here.
 */
@Service
public class EventService
{
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository)
    {
        this.eventRepository = eventRepository;
    }

    /**
     * Converts the API request object into the database entity shape
     * and persists it using the repository.
     */
    public EventEntity createEvent(CreateEventRequest request)
    {
        EventEntity entity = new EventEntity();
        entity.setGroupCode(request.getGroupCode());
        entity.setPlayerName(request.getPlayerName());
        entity.setEventType(request.getEventType());
        entity.setEventTime(request.getEventTime());
        entity.setPayloadJson(request.getPayloadJson());
        return eventRepository.save(entity);
    }
}
