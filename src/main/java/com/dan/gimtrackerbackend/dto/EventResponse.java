package com.dan.gimtrackerbackend.dto;

import com.dan.gimtrackerbackend.model.EventEntity;

import java.time.Instant;

/**
 * Response shape returned to the client after an event is saved.
 * This lets the API return persisted values such as the generated id
 * and backend-created timestamp without exposing the entity directly.
 */
public class EventResponse
{
    private Long id;
    private String groupCode;
    private String playerName;
    private String eventType;
    private Instant eventTime;
    private String payloadJson;
    private Instant createdAt;

    /**
     * Builds an API response object from the saved JPA entity.
     */
    public static EventResponse fromEntity(EventEntity entity)
    {
        EventResponse response = new EventResponse();
        response.id = entity.getId();
        response.groupCode = entity.getGroupCode();
        response.playerName = entity.getPlayerName();
        response.eventType = entity.getEventType();
        response.eventTime = entity.getEventTime();
        response.payloadJson = entity.getPayloadJson();
        response.createdAt = entity.getCreatedAt();
        return response;
    }

    public Long getId()
    {
        return id;
    }

    public String getGroupCode()
    {
        return groupCode;
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public String getEventType()
    {
        return eventType;
    }

    public Instant getEventTime()
    {
        return eventTime;
    }

    public String getPayloadJson()
    {
        return payloadJson;
    }

    public Instant getCreatedAt()
    {
        return createdAt;
    }
}
