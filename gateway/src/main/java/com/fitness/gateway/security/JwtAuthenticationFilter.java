package com.fitness.gateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Gateway-level authentication filter. Runs on every request and:
 *   1. rejects requests with a missing, malformed, expired or wrongly-signed JWT (401);
 *   2. forwards the caller's id downstream as the X-User-ID header, overwriting any value the
 *      client sent, so activityservice and aiservice never have to handle JWTs themselves.
 *
 * /api/users/register and /api/users/login are public: no token exists yet when they are
 * called - the token is their result.
 *
 * CORS preflight requests (the browser's automatic OPTIONS request before a cross-origin call)
 * never carry an Authorization header, so they are passed through untouched; the CORS filter
 * answers them. The real request that follows is still fully authenticated.
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // always after CorsConfig's filter, which runs first
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/users/register",
            "/api/users/login"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (CorsUtils.isPreFlightRequest(exchange.getRequest())) {
            return chain.filter(exchange);
        }

        if (PUBLIC_PATHS.contains(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(7);
        String userId = jwtUtil.validateAndGetUserId(token);

        if (userId == null) {
            return unauthorized(exchange, "Invalid or expired token");
        }

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-ID", userId)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        log.warn("Rejected request to {}: {}", exchange.getRequest().getURI().getPath(), message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        DataBuffer buffer = bufferFactory.wrap(
                ("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8)
        );
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
