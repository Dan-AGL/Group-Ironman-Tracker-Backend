package com.dan.gimtrackerbackend.api;

import com.dan.gimtrackerbackend.dto.CreateEventRequest;
import com.dan.gimtrackerbackend.dto.EventResponse;
import com.dan.gimtrackerbackend.model.EventEntity;
import com.dan.gimtrackerbackend.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Entry point for event data coming into the backend.
 * The RuneLite-side client will eventually send HTTP requests here,
 * and this controller hands the validated request off to the service layer.
 */
@RestController
@RequestMapping("/api/events")
public class EventController
{
    private final EventService eventService;

    public EventController(EventService eventService)
    {
        this.eventService = eventService;
    }

    /**
     * Returns all events stored for one group.
     * The controller receives the path parameter, delegates the lookup to the
     * service, and converts each entity into a response DTO for the API caller.
     */
    @GetMapping("/group/{groupCode}")
    public List<EventResponse> getEventsByGroupCode(@PathVariable String groupCode)
    {
        return eventService.getEventsByGroupCode(groupCode)
            .stream()
            .map(EventResponse::fromEntity)
            .toList();
    }

    /**
     * Accepts one event from the client, validates the request body,
     * saves it through the service layer, and returns the saved record.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@Valid @RequestBody CreateEventRequest request)
    {
        EventEntity savedEvent = eventService.createEvent(request);
        return EventResponse.fromEntity(savedEvent);
    }
}
