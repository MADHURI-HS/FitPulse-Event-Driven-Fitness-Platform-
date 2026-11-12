package com.fitness.gateway;

import com.fitness.gateway.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.Order;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression guard: the CORS filter must run before the JWT filter, otherwise the browser's
 * preflight requests are rejected with 401 and no CORS headers.
 */
class FilterOrderTest {

    @Test
    void corsFilterRunsBeforeJwtFilter() throws NoSuchMethodException {
        Order cors = CorsConfig.class.getMethod("corsWebFilter").getAnnotation(Order.class);
        Order jwt = JwtAuthenticationFilter.class.getAnnotation(Order.class);

        assertNotNull(cors, "CorsConfig.corsWebFilter must declare an explicit @Order");
        assertNotNull(jwt, "JwtAuthenticationFilter must declare an explicit @Order");
        assertTrue(cors.value() < jwt.value(), "CORS filter must have higher precedence than the JWT filter");
    }
}
