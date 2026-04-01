package com.dan.gimtrackerbackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Batched progress upload contract used by the RuneLite plugin.
 */
public class ProgressUploadRequest
{
    @NotBlank
    private String groupCode;

    @NotBlank
    private String playerName;

    @NotBlank
    private String timestamp;

    @Valid
    @NotEmpty
    private List<ProgressEventRequest> events;

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

    public String getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(String timestamp)
    {
        this.timestamp = timestamp;
    }

    public List<ProgressEventRequest> getEvents()
    {
        return events;
    }

    public void setEvents(List<ProgressEventRequest> events)
    {
        this.events = events;
    }
}
