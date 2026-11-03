package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityMessageListenerTest {

    @Mock private ActivityAIService aiService;
    @Mock private RecommendationRepository repository;
    @InjectMocks private ActivityMessageListener listener;

    private Activity activity() {
        Activity a = new Activity();
        a.setId("a1");
        a.setUserId("u1");
        return a;
    }

    @Test
    void newActivity_generatesAndSavesRecommendation() {
        Recommendation rec = Recommendation.builder().activityId("a1").build();
        when(repository.existsByActivityId("a1")).thenReturn(false);
        when(aiService.generateRecommendation(any(Activity.class))).thenReturn(rec);

        listener.processActivity(activity());

        verify(repository).save(rec);
    }

    @Test
    void redeliveredActivity_isSkipped() {
        when(repository.existsByActivityId("a1")).thenReturn(true);

        listener.processActivity(activity());

        verifyNoInteractions(aiService);
        verify(repository, never()).save(any());
    }

    @Test
    void aiFailure_propagatesSoRabbitRetriesAndDeadLetters() {
        when(repository.existsByActivityId("a1")).thenReturn(false);
        when(aiService.generateRecommendation(any(Activity.class))).thenThrow(new IllegalStateException("gemini down"));

        assertThrows(IllegalStateException.class, () -> listener.processActivity(activity()));
        verify(repository, never()).save(any());
    }
}
