package com.dan.gimtrackerbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "events")
public class EventEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String groupCode;

    @Column(nullable = false)
    private String playerName;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private Instant eventTime;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist()
    {
        createdAt = Instant.now();
    }

    public Long getId()
    {
        return id;
    }

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

    public Instant getCreatedAt()
    {
        return createdAt;
    }
}
