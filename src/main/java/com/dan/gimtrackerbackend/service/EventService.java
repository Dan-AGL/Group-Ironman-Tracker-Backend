package com.dan.gimtrackerbackend.service;

import com.dan.gimtrackerbackend.dto.CreateEventRequest;
import com.dan.gimtrackerbackend.dto.ProgressEventRequest;
import com.dan.gimtrackerbackend.dto.ProgressUploadRequest;
import com.dan.gimtrackerbackend.model.EventEntity;
import com.dan.gimtrackerbackend.model.GroupEntity;
import com.dan.gimtrackerbackend.repository.EventRepository;
import com.dan.gimtrackerbackend.repository.GroupMemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private final GroupService groupService;
    private final GroupMemberRepository groupMemberRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EventService(EventRepository eventRepository, GroupService groupService, GroupMemberRepository groupMemberRepository)
    {
        this.eventRepository = eventRepository;
        this.groupService = groupService;
        this.groupMemberRepository = groupMemberRepository;
    }

    /**
     * Reads all stored events for a specific group code.
     * Keeping this in the service means controller code stays thin and
     * future filtering or authorization rules can be added in one place.
     */
    public List<EventEntity> getEventsByGroupCode(String groupCode)
    {
        List<EventEntity> groupedEvents = eventRepository.findByGroupInviteCodeOrderByEventTimeAsc(groupCode);
        if (!groupedEvents.isEmpty())
        {
            return groupedEvents;
        }

        return eventRepository.findByGroupCodeOrderByEventTimeAsc(groupCode);
    }

    /**
     * Converts the API request object into the database entity shape
     * and persists it using the repository.
     */
    public EventEntity createEvent(CreateEventRequest request)
    {
        GroupEntity group = requireMemberGroup(request.getGroupCode(), request.getPlayerName());
        EventEntity entity = new EventEntity();
        entity.setGroup(group);
        entity.setGroupCode(group.getInviteCode());
        entity.setPlayerName(request.getPlayerName());
        entity.setEventType(request.getEventType());
        entity.setEventTime(request.getEventTime());
        entity.setPayloadJson(request.getPayloadJson());
        return eventRepository.save(entity);
    }

    /**
     * Converts the plugin's batched progress payload into one stored row per tracked event.
     */
    public List<EventEntity> createEventsFromProgressUpload(ProgressUploadRequest request)
    {
        return request.getEvents()
            .stream()
            .map(event -> buildProgressEventEntity(request, event))
            .map(eventRepository::save)
            .toList();
    }

    private EventEntity buildProgressEventEntity(ProgressUploadRequest request, ProgressEventRequest event)
    {
        GroupEntity group = requireMemberGroup(request.getGroupCode(), request.getPlayerName());
        EventEntity entity = new EventEntity();
        entity.setGroup(group);
        entity.setGroupCode(group.getInviteCode());
        entity.setPlayerName(request.getPlayerName());
        entity.setEventType(event.getType());
        entity.setEventTime(parseEventTime(event.getTimestamp(), request.getTimestamp()));
        entity.setPayloadJson(serializeProgressPayload(event));
        return entity;
    }

    private Instant parseEventTime(String eventTimestamp, String uploadTimestamp)
    {
        if (eventTimestamp != null && !eventTimestamp.isBlank())
        {
            return Instant.parse(eventTimestamp);
        }

        return Instant.parse(uploadTimestamp);
    }

    private String serializeProgressPayload(ProgressEventRequest event)
    {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("summary", event.getSummary());
        payload.put("details", event.getDetails());

        try
        {
            return objectMapper.writeValueAsString(payload);
        }
        catch (JsonProcessingException ex)
        {
            throw new IllegalArgumentException("Unable to serialize progress event payload", ex);
        }
    }

    private GroupEntity requireMemberGroup(String inviteCode, String playerName)
    {
        GroupEntity group = groupService.getGroupByInviteCode(inviteCode);
        boolean isMember = groupMemberRepository.findByGroupAndPlayerNameIgnoreCase(group, playerName.trim()).isPresent();
        if (!isMember)
        {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Player is not a member of this group");
        }

        return group;
    }
}
