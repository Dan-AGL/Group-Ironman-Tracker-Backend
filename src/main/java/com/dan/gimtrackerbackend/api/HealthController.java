package com.dan.gimtrackerbackend.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Simple diagnostic endpoint used to confirm that the backend is running
 * and able to serve HTTP requests.
 */
@RestController
public class HealthController
{
    /**
     * Returns a small static payload so callers can verify the service is up.
     */
    @GetMapping("/api/health")
    public Map<String, String> health()
    {
        return Map.of("status", "ok");
    }

    /**
     * Compatibility health endpoint used by the original plugin README and mock backend.
     */
    @GetMapping("/health")
    public Map<String, String> legacyHealth()
    {
        return health();
    }
}
