package com.dan.gimtrackerbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Request body expected by POST /api/events.
 * This is separate from the entity so the API contract can evolve
 * without exposing the database model directly.
 */
public class CreateEventRequest
{
    /**
     * Group identifier supplied by the client.
     */
    @NotBlank
    private String groupCode;

    /**
     * Player name supplied by the client.
     */
    @NotBlank
    private String playerName;

    /**
     * Event category supplied by the client.
     */
    @NotBlank
    private String eventType;

    /**
     * Time the client says the event occurred.
     */
    @NotNull
    private Instant eventTime;

    /**
     * Extra event details serialized as JSON text.
     */
    @NotBlank
    private String payloadJson;

    public String getGroupCode()
    {
        return groupCode;
    }

    public void setGroupCode(String groupCode)
    {
        this.groupCode = groupCode;
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public void setPlayerName(String playerName)
    {
        this.playerName = playerName;
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public Instant getEventTime()
    {
        return eventTime;
    }

    public void setEventTime(Instant eventTime)
    {
        this.eventTime = eventTime;
    }

    public String getPayloadJson()
    {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson)
    {
        this.payloadJson = payloadJson;
    }
}
