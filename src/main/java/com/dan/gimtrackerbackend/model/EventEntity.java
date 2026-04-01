package com.dan.gimtrackerbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * JPA entity that maps directly to the events table in PostgreSQL.
 * Each row represents one tracker event sent from a client.
 */
@Entity
@Table(name = "events")
public class EventEntity
{
    /**
     * Database-generated primary key for the row.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifies which group ironman group this event belongs to.
     */
    @Column(nullable = false)
    private String groupCode;

    /**
     * Name of the player who triggered the event.
     */
    @Column(nullable = false)
    private String playerName;

    /**
     * High-level event category, for example ITEM_OBTAINED.
     */
    @Column(nullable = false)
    private String eventType;

    /**
     * The actual time the event happened in-game or on the client.
     */
    @Column(nullable = false)
    private Instant eventTime;

    /**
     * Raw event payload stored as JSON text so the schema can stay flexible
     * while the project is still evolving.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    /**
     * Timestamp for when the backend saved the record.
     */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Automatically sets the database creation timestamp just before insert.
     */
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
