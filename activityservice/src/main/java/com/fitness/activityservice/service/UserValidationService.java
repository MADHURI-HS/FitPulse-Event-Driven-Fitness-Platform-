package com.fitness.activityservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserValidationService {
    private final WebClient userServiceWebClient;

    /**
     * @return true if the user exists, false if user-service says it does not.
     * @throws IllegalStateException if user-service cannot be reached or fails (mapped to HTTP 503).
     */
    public boolean validateUser(String userId) {
        log.info("Calling User Validation API for userId: {}", userId);
        try {
            return Boolean.TRUE.equals(userServiceWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .timeout(Duration.ofSeconds(5))
                    .block());
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND || e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return false;
            }
            throw new IllegalStateException("User service returned an error: " + e.getStatusCode(), e);
        } catch (WebClientRequestException e) {
            throw new IllegalStateException("User service is unavailable", e);
        } catch (RuntimeException e) {
            // includes reactor timeout
            throw new IllegalStateException("User service did not respond in time", e);
        }
    }
}
