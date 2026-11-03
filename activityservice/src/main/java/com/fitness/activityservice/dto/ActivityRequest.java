package com.fitness.activityservice.dto;

import com.fitness.activityservice.model.ActivityType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ActivityRequest {
    // Always overwritten from the gateway's X-User-ID header - never trusted from the body.
    private String userId;

    @NotNull(message = "Activity type is required")
    private ActivityType type;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be greater than 0")
    private Integer duration;

    @PositiveOrZero(message = "Calories burned cannot be negative")
    private Integer caloriesBurned;

    private LocalDateTime startTime;
    private Map<String, Object> additionalMetrics;
}
