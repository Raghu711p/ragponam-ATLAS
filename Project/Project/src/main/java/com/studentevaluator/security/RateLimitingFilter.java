package com.studentevaluator.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentevaluator.dto.ErrorResponse;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Rate limiting filter to prevent abuse and DoS attacks.
 * Implements sliding window rate limiting per IP address.
 */
@Component
public class RateLimitingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
    
    @Value("${security.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;
    
    @Value("${security.rate-limit.enabled:true}")
    private boolean rateLimitingEnabled;
    
    private final ConcurrentMap<String, ClientRequestInfo> clientRequests = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (!rateLimitingEnabled) {
            chain.doFilter(request, response);
            return;
        }
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIp = getClientIpAddress(httpRequest);
        String requestUri = httpRequest.getRequestURI();
        
        // Skip rate limiting for health check endpoints
        if (isHealthCheckEndpoint(requestUri)) {
            chain.doFilter(request, response);
            return;
        }
        
        if (isRateLimitExceeded(clientIp)) {
            logger.warn("Rate limit exceeded for IP: {} on URI: {}", clientIp, requestUri);
            sendRateLimitResponse(httpResponse);
            return;
        }
        
        // Record the request
        recordRequest(clientIp);
        
        chain.doFilter(request, response);
    }
    
    private boolean isRateLimitExceeded(String clientIp) {
        ClientRequestInfo requestInfo = clientRequests.get(clientIp);
        
        if (requestInfo == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minus(1, ChronoUnit.MINUTES);
        
        // Clean up old requests outside the window
        requestInfo.getRequestTimes().removeIf(time -> time.isBefore(windowStart));
        
        return requestInfo.getRequestTimes().size() >= requestsPerMinute;
    }
    
    private void recordRequest(String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        
        clientRequests.compute(clientIp, (ip, requestInfo) -> {
            if (requestInfo == null) {
                requestInfo = new ClientRequestInfo();
            }
            requestInfo.getRequestTimes().add(now);
            return requestInfo;
        });
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private boolean isHealthCheckEndpoint(String requestUri) {
        return requestUri.startsWith("/actuator/health") || 
               requestUri.equals("/health") ||
               requestUri.equals("/ping");
    }
    
    private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "RATE_LIMIT_EXCEEDED",
            "Too many requests. Please try again later."
        );
        
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
    
    /**
     * Clean up old request records periodically to prevent memory leaks.
     */
    public void cleanupOldRecords() {
        LocalDateTime cutoff = LocalDateTime.now().minus(5, ChronoUnit.MINUTES);
        
        clientRequests.entrySet().removeIf(entry -> {
            ClientRequestInfo requestInfo = entry.getValue();
            requestInfo.getRequestTimes().removeIf(time -> time.isBefore(cutoff));
            return requestInfo.getRequestTimes().isEmpty();
        });
        
        logger.debug("Cleaned up old rate limiting records. Active clients: {}", clientRequests.size());
    }
    
    /**
     * Get current rate limiting statistics.
     */
    public RateLimitStats getStats() {
        int activeClients = clientRequests.size();
        int totalRequests = clientRequests.values().stream()
                .mapToInt(info -> info.getRequestTimes().size())
                .sum();
        
        return new RateLimitStats(activeClients, totalRequests, requestsPerMinute);
    }
    
    // Inner classes
    
    private static class ClientRequestInfo {
        private final java.util.List<LocalDateTime> requestTimes = new java.util.concurrent.CopyOnWriteArrayList<>();
        
        public java.util.List<LocalDateTime> getRequestTimes() {
            return requestTimes;
        }
    }
    
    public static class RateLimitStats {
        private final int activeClients;
        private final int totalRequests;
        private final int requestsPerMinute;
        
        public RateLimitStats(int activeClients, int totalRequests, int requestsPerMinute) {
            this.activeClients = activeClients;
            this.totalRequests = totalRequests;
            this.requestsPerMinute = requestsPerMinute;
        }
        
        public int getActiveClients() { return activeClients; }
        public int getTotalRequests() { return totalRequests; }
        public int getRequestsPerMinute() { return requestsPerMinute; }
    }
}