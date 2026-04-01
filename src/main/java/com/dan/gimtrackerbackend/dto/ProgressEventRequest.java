package com.dan.gimtrackerbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * One tracked event inside the plugin's batch upload payload.
 */
public class ProgressEventRequest
{
    @NotBlank
    private String type;

    @NotBlank
    private String timestamp;

    @NotBlank
    private String summary;

    @NotNull
    private Map<String, Object> details;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(String timestamp)
    {
        this.timestamp = timestamp;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public Map<String, Object> getDetails()
    {
        return details;
    }

    public void setDetails(Map<String, Object> details)
    {
        this.details = details;
    }
}
