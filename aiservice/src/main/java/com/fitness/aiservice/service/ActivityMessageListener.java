package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    private final ActivityAIService aiService;
    private final RecommendationRepository recommendationRepository;

    /**
     * If this throws (e.g. Gemini is down), the listener retries with backoff (see ai-service.yml)
     * and then dead-letters the message to activity.dlq.
     */
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void processActivity(Activity activity) {
        log.info("Received activity for processing: {}", activity.getId());

        // RabbitMQ is at-least-once: a redelivered message must not create a second recommendation.
        if (recommendationRepository.existsByActivityId(activity.getId())) {
            log.info("Recommendation already exists for activity {} - skipping duplicate", activity.getId());
            return;
        }

        Recommendation recommendation = aiService.generateRecommendation(activity);
        recommendationRepository.save(recommendation);
    }
}
