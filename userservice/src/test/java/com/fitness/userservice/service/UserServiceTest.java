package com.fitness.userservice.service;

import com.fitness.userservice.dto.AuthResponse;
import com.fitness.userservice.dto.LoginRequest;
import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.exception.ResourceNotFoundException;
import com.fitness.userservice.model.User;
import com.fitness.userservice.repository.UserRepository;
import com.fitness.userservice.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository repository;
    @Mock private JwtUtil jwtUtil;
    @InjectMocks private UserService userService;

    private RegisterRequest registerRequest() {
        RegisterRequest r = new RegisterRequest();
        r.setEmail("a@b.com");
        r.setPassword("secret123");
        r.setFirstName("Asha");
        return r;
    }

    @Test
    void register_hashesPasswordWithBCryptAndReturnsToken() {
        when(repository.existsByEmail("a@b.com")).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId("u1");
            return u;
        });
        when(jwtUtil.generateToken("u1", "a@b.com")).thenReturn("token");

        AuthResponse res = userService.register(registerRequest());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        String stored = captor.getValue().getPassword();
        assertNotEquals("secret123", stored);
        assertTrue(new BCryptPasswordEncoder().matches("secret123", stored));
        assertEquals("token", res.getToken());
        assertEquals("u1", res.getUserId());
    }

    @Test
    void register_duplicateEmail_throwsAndDoesNotSave() {
        when(repository.existsByEmail("a@b.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.register(registerRequest()));
        verify(repository, never()).save(any());
    }

    @Test
    void login_correctPassword_returnsToken() {
        User u = new User();
        u.setId("u1");
        u.setEmail("a@b.com");
        u.setPassword(new BCryptPasswordEncoder().encode("right"));
        when(repository.findByEmail("a@b.com")).thenReturn(u);
        when(jwtUtil.generateToken("u1", "a@b.com")).thenReturn("token");

        LoginRequest req = new LoginRequest();
        req.setEmail("a@b.com");
        req.setPassword("right");

        assertEquals("token", userService.login(req).getToken());
    }

    @Test
    void login_wrongPassword_throws() {
        User u = new User();
        u.setId("u1");
        u.setEmail("a@b.com");
        u.setPassword(new BCryptPasswordEncoder().encode("right"));
        when(repository.findByEmail("a@b.com")).thenReturn(u);

        LoginRequest req = new LoginRequest();
        req.setEmail("a@b.com");
        req.setPassword("wrong");

        assertThrows(IllegalArgumentException.class, () -> userService.login(req));
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_unknownEmail_throws() {
        when(repository.findByEmail("x@y.com")).thenReturn(null);

        LoginRequest req = new LoginRequest();
        req.setEmail("x@y.com");
        req.setPassword("whatever");

        assertThrows(IllegalArgumentException.class, () -> userService.login(req));
    }

    @Test
    void getUserProfile_existingUser_doesNotExposePassword() {
        User u = new User();
        u.setId("u1");
        u.setEmail("a@b.com");
        u.setPassword("hash");
        when(repository.findById("u1")).thenReturn(Optional.of(u));

        UserResponse res = userService.getUserProfile("u1");

        assertEquals("a@b.com", res.getEmail());
    }

    @Test
    void getUserProfile_missingUser_throwsNotFound() {
        when(repository.findById("nope")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserProfile("nope"));
    }
}
