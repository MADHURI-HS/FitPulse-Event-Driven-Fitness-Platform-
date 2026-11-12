package com.fitness.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "this-is-a-test-secret-key-at-least-32-bytes-long!";
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(new JwtUtil(SECRET));

    private String token(String secret, String subject, long ttlMs) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder().subject(subject)
                .expiration(new Date(System.currentTimeMillis() + ttlMs))
                .signWith(key).compact();
    }

    private ServerWebExchange run(MockServerHttpRequest.BaseBuilder<?> request, AtomicReference<ServerWebExchange> passedOn) {
        MockServerWebExchange exchange = MockServerWebExchange.from(request.build());
        WebFilterChain chain = e -> {
            passedOn.set(e);
            return Mono.empty();
        };
        filter.filter(exchange, chain).block();
        return exchange;
    }

    @Test
    void missingAuthorizationHeader_isRejectedWith401() {
        AtomicReference<ServerWebExchange> passedOn = new AtomicReference<>();
        ServerWebExchange ex = run(MockServerHttpRequest.get("/api/activities"), passedOn);

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getResponse().getStatusCode());
        assertNull(passedOn.get());
    }

    @Test
    void invalidToken_isRejectedWith401() {
        AtomicReference<ServerWebExchange> passedOn = new AtomicReference<>();
        ServerWebExchange ex = run(MockServerHttpRequest.get("/api/activities")
                .header("Authorization", "Bearer not.a.jwt"), passedOn);

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getResponse().getStatusCode());
        assertNull(passedOn.get());
    }

    @Test
    void expiredToken_isRejectedWith401() {
        AtomicReference<ServerWebExchange> passedOn = new AtomicReference<>();
        ServerWebExchange ex = run(MockServerHttpRequest.get("/api/activities")
                .header("Authorization", "Bearer " + token(SECRET, "u1", -5_000)), passedOn);

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getResponse().getStatusCode());
    }

    @Test
    void tokenSignedWithWrongSecret_isRejectedWith401() {
        AtomicReference<ServerWebExchange> passedOn = new AtomicReference<>();
        String other = "another-completely-different-secret-key-32-bytes!!";
        ServerWebExchange ex = run(MockServerHttpRequest.get("/api/activities")
                .header("Authorization", "Bearer " + token(other, "u1", 60_000)), passedOn);

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getResponse().getStatusCode());
    }

    @Test
    void validToken_forwardsUserIdHeader() {
        AtomicReference<ServerWebExchange> passedOn = new AtomicReference<>();
        run(MockServerHttpRequest.get("/api/activities")
                .header("Authorization", "Bearer " + token(SECRET, "u1", 60_000)), passedOn);

        assertNotNull(passedOn.get());
        assertEquals("u1", passedOn.get().getRequest().getHeaders().getFirst("X-User-ID"));
    }

    @Test
    void spoofedUserIdHeader_isOverwrittenWithTokenSubject() {
        AtomicReference<ServerWebExchange> passedOn = new AtomicReference<>();
        run(MockServerHttpRequest.get("/api/activities")
                .header("Authorization", "Bearer " + token(SECRET, "real-user", 60_000))
                .header("X-User-ID", "someone-else"), passedOn);

        assertEquals("real-user", passedOn.get().getRequest().getHeaders().getFirst("X-User-ID"));
    }

    @Test
    void loginAndRegister_arePublic() {
        AtomicReference<ServerWebExchange> passedOn = new AtomicReference<>();
        run(MockServerHttpRequest.post("/api/users/login"), passedOn);
        assertNotNull(passedOn.get());

        passedOn.set(null);
        run(MockServerHttpRequest.post("/api/users/register"), passedOn);
        assertNotNull(passedOn.get());
    }

    @Test
    void corsPreflight_isPassedThroughWithoutAToken() {
        AtomicReference<ServerWebExchange> passedOn = new AtomicReference<>();
        ServerWebExchange ex = run(MockServerHttpRequest.options("http://localhost:8080/api/activities")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "authorization,content-type"), passedOn);

        assertNotNull(passedOn.get());
        assertNotEquals(HttpStatus.UNAUTHORIZED, ex.getResponse().getStatusCode());
    }

    @Test
    void plainOptionsRequest_withoutCorsHeaders_isStillRejected() {
        AtomicReference<ServerWebExchange> passedOn = new AtomicReference<>();
        ServerWebExchange ex = run(MockServerHttpRequest.options("http://localhost:8080/api/activities"), passedOn);

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getResponse().getStatusCode());
        assertNull(passedOn.get());
    }
}
