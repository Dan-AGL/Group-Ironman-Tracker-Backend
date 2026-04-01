package com.dan.gimtrackerbackend.api;

import com.dan.gimtrackerbackend.dto.ProgressUploadRequest;
import com.dan.gimtrackerbackend.model.EventEntity;
import com.dan.gimtrackerbackend.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Compatibility controller for the RuneLite plugin's existing batch upload route.
 */
@RestController
public class ProgressController
{
    private final EventService eventService;

    public ProgressController(EventService eventService)
    {
        this.eventService = eventService;
    }

    @PostMapping("/api/progress")
    public ResponseEntity<Map<String, Object>> createProgressEvents(@Valid @RequestBody ProgressUploadRequest request)
    {
        List<EventEntity> savedEvents = eventService.createEventsFromProgressUpload(request);
        return ResponseEntity.ok(Map.of("ok", true, "storedCount", savedEvents.size()));
    }
}
