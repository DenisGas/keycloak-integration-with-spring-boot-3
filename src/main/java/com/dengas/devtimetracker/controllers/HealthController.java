package com.dengas.devtimetracker.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Application health check endpoint")
public class HealthController {

    @Operation(
            summary = "Check application health status",
            description = "Returns a simple status response to verify if the application is running."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application is up and running")
    })
    @GetMapping
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "timestamp", System.currentTimeMillis()
        );
    }
}
