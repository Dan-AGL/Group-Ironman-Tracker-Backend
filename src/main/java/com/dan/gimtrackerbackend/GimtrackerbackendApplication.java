package com.dan.gimtrackerbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot entry point.
 * Running this class starts the embedded web server, creates the Spring
 * application context, and wires together controllers, services, and repositories.
 */
@SpringBootApplication
public class GimtrackerbackendApplication
{
    /**
     * Launches the backend application.
     */
    public static void main(String[] args)
    {
        SpringApplication.run(GimtrackerbackendApplication.class, args);
    }
}
