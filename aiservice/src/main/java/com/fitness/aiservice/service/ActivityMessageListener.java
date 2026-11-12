package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
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
        // This check is the fast path; the unique index on activityId is the real guarantee.
        if (recommendationRepository.existsByActivityId(activity.getId())) {
            log.info("Recommendation already exists for activity {} - skipping duplicate", activity.getId());
            return;
        }

        Recommendation recommendation = aiService.generateRecommendation(activity);
        try {
            recommendationRepository.save(recommendation);
        } catch (DuplicateKeyException e) {
            // Another delivery of the same message saved it between the check above and this insert
            // (unique index on activityId). The work is done, so acknowledge instead of retrying.
            log.info("Recommendation for activity {} was saved concurrently - treating as duplicate", activity.getId());
        }
    }
}
