package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityAIServiceTest {

    @Mock private GeminiService geminiService;
    @InjectMocks private ActivityAIService aiService;

    private Activity activity() {
        Activity a = new Activity();
        a.setId("a1");
        a.setUserId("u1");
        a.setType("RUNNING");
        a.setDuration(30);
        a.setCaloriesBurned(300);
        return a;
    }

    @Test
    void malformedGeminiResponse_fallsBackToDefaultRecommendation() {
        when(geminiService.getAnswer(anyString())).thenReturn("this is not json");

        Recommendation r = aiService.generateRecommendation(activity());

        assertEquals("a1", r.getActivityId());
        assertEquals("u1", r.getUserId());
        assertEquals("Unable to generate detailed analysis", r.getRecommendation());
        assertFalse(r.getSafety().isEmpty());
    }

    @Test
    void emptyCandidates_fallsBackToDefaultRecommendation() {
        when(geminiService.getAnswer(anyString())).thenReturn("{\"candidates\":[]}");

        Recommendation r = aiService.generateRecommendation(activity());

        assertEquals("Unable to generate detailed analysis", r.getRecommendation());
    }

    @Test
    void validGeminiResponse_isParsedIntoRecommendation() {
        String inner = "{\\\"analysis\\\":{\\\"overall\\\":\\\"Good pace\\\"},"
                + "\\\"improvements\\\":[{\\\"area\\\":\\\"Cadence\\\",\\\"recommendation\\\":\\\"Increase cadence\\\"}],"
                + "\\\"suggestions\\\":[{\\\"workout\\\":\\\"Intervals\\\",\\\"description\\\":\\\"4x400m\\\"}],"
                + "\\\"safety\\\":[\\\"Warm up\\\"]}";
        String response = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"" + inner + "\"}]}}]}";
        when(geminiService.getAnswer(anyString())).thenReturn(response);

        Recommendation r = aiService.generateRecommendation(activity());

        assertEquals("a1", r.getActivityId());
        assertEquals("RUNNING", r.getActivityType());
        assertTrue(r.getRecommendation().contains("Good pace"));
        assertTrue(r.getSafety().contains("Warm up"));
    }
}
