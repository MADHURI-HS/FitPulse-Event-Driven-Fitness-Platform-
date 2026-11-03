package com.fitness.aiservice.controller;

import com.fitness.aiservice.exception.ForbiddenException;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Recommendation>> getUserRecommendation(@PathVariable String userId,
                                                                      @RequestHeader("X-User-ID") String callerId) {
        if (!userId.equals(callerId)) {
            throw new ForbiddenException("You can only view your own recommendations");
        }
        return ResponseEntity.ok(recommendationService.getUserRecommendation(userId));
    }

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<Recommendation> getActivityRecommendation(@PathVariable String activityId,
                                                                    @RequestHeader("X-User-ID") String callerId) {
        Recommendation recommendation = recommendationService.getActivityRecommendation(activityId);
        if (!recommendation.getUserId().equals(callerId)) {
            throw new ForbiddenException("You do not have access to this recommendation");
        }
        return ResponseEntity.ok(recommendation);
    }
}
