package com.fitness.aiservice.controller;

import com.fitness.aiservice.exception.ForbiddenException;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationControllerTest {

    @Mock private RecommendationService service;
    @InjectMocks private RecommendationController controller;

    @Test
    void ownRecommendations_areReturned() {
        when(service.getUserRecommendation("u1")).thenReturn(List.of(new Recommendation()));

        assertEquals(1, controller.getUserRecommendation("u1", "u1").getBody().size());
    }

    @Test
    void otherUsersRecommendations_areForbidden() {
        assertThrows(ForbiddenException.class, () -> controller.getUserRecommendation("victim", "attacker"));
        verifyNoInteractions(service);
    }

    @Test
    void activityRecommendation_ofAnotherUser_isForbidden() {
        Recommendation rec = Recommendation.builder().activityId("a1").userId("owner").build();
        when(service.getActivityRecommendation("a1")).thenReturn(rec);

        assertThrows(ForbiddenException.class, () -> controller.getActivityRecommendation("a1", "intruder"));
    }

    @Test
    void activityRecommendation_ofOwner_isReturned() {
        Recommendation rec = Recommendation.builder().activityId("a1").userId("owner").build();
        when(service.getActivityRecommendation("a1")).thenReturn(rec);

        assertSame(rec, controller.getActivityRecommendation("a1", "owner").getBody());
    }
}
