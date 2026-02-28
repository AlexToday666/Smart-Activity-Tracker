package com.example.ActivityTracker.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    public static final String REQUEST_ID = "requestId";
    public static final String CORRELATION_ID = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = firstHeader(request, "X-Request-Id", UUID.randomUUID().toString());
        String correlationId = firstHeader(request, "X-Correlation-Id", requestId);
        try {
            MDC.put(REQUEST_ID, requestId);
            MDC.put(CORRELATION_ID, correlationId);
            response.setHeader("X-Request-Id", requestId);
            response.setHeader("X-Correlation-Id", correlationId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String firstHeader(HttpServletRequest request, String name, String fallback) {
        String value = request.getHeader(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
