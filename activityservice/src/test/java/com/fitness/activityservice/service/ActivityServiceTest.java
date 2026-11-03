package com.fitness.activityservice.service;

import com.fitness.activityservice.ActivityRepository;
import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.exception.ForbiddenException;
import com.fitness.activityservice.exception.ResourceNotFoundException;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.model.ActivityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock private ActivityRepository activityRepository;
    @Mock private UserValidationService userValidationService;
    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private ActivityService activityService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(activityService, "exchange", "fitness.exchange");
        ReflectionTestUtils.setField(activityService, "routingKey", "activity.tracking");
    }

    private ActivityRequest request() {
        ActivityRequest r = new ActivityRequest();
        r.setUserId("u1");
        r.setType(ActivityType.RUNNING);
        r.setDuration(30);
        r.setCaloriesBurned(300);
        return r;
    }

    @Test
    void trackActivity_savesAndPublishesEvent() {
        when(userValidationService.validateUser("u1")).thenReturn(true);
        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> i.getArgument(0));

        ActivityResponse response = activityService.trackActivity(request());

        assertEquals("u1", response.getUserId());
        assertEquals(ActivityType.RUNNING, response.getType());
        verify(rabbitTemplate).convertAndSend(eq("fitness.exchange"), eq("activity.tracking"), any(Activity.class));
    }

    @Test
    void trackActivity_invalidUser_throwsAndDoesNotSaveOrPublish() {
        when(userValidationService.validateUser("u1")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> activityService.trackActivity(request()));
        verify(activityRepository, never()).save(any());
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void trackActivity_brokerDown_activityIsStillSaved() {
        when(userValidationService.validateUser("u1")).thenReturn(true);
        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("broker down"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Activity.class));

        assertDoesNotThrow(() -> activityService.trackActivity(request()));
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    void getUserActivities_returnsMappedActivitiesForUser() {
        Activity a = Activity.builder().id("a1").userId("u1").type(ActivityType.RUNNING).build();
        when(activityRepository.findByUserIdOrderByStartTimeDesc("u1")).thenReturn(List.of(a));

        List<ActivityResponse> result = activityService.getUserActivities("u1");

        assertEquals(1, result.size());
        assertEquals("a1", result.get(0).getId());
    }

    @Test
    void getActivityById_otherUsersActivity_isForbidden() {
        Activity a = Activity.builder().id("a1").userId("owner").build();
        when(activityRepository.findById("a1")).thenReturn(Optional.of(a));

        assertThrows(ForbiddenException.class, () -> activityService.getActivityById("a1", "intruder"));
    }

    @Test
    void getActivityById_owner_getsActivity() {
        Activity a = Activity.builder().id("a1").userId("owner").build();
        when(activityRepository.findById("a1")).thenReturn(Optional.of(a));

        assertEquals("a1", activityService.getActivityById("a1", "owner").getId());
    }

    @Test
    void getActivityById_missing_throwsNotFound() {
        when(activityRepository.findById("nope")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> activityService.getActivityById("nope", "u1"));
    }
}
