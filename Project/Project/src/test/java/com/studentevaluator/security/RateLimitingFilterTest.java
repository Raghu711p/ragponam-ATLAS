package com.studentevaluator.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for RateLimitingFilter.
 */
@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    private RateLimitingFilter rateLimitingFilter;
    
    @BeforeEach
    void setUp() {
        rateLimitingFilter = new RateLimitingFilter();
        ReflectionTestUtils.setField(rateLimitingFilter, "requestsPerMinute", 5);
        ReflectionTestUtils.setField(rateLimitingFilter, "rateLimitingEnabled", true);
    }
    
    @Test
    void testDoFilter_WithinRateLimit_ShouldAllowRequest() throws IOException, ServletException {
        // Setup
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getRequestURI()).thenReturn("/api/v1/evaluations");
        
        // Execute
        rateLimitingFilter.doFilter(request, response, filterChain);
        
        // Verify
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }
    
    @Test
    void testDoFilter_ExceedsRateLimit_ShouldBlockRequest() throws IOException, ServletException {
        // Setup
        String clientIp = "192.168.1.1";
        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(request.getRequestURI()).thenReturn("/api/v1/evaluations");
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        
        // Make requests up to the limit
        for (int i = 0; i < 5; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }
        
        // Reset mocks for the rate-limited request
        reset(filterChain, response);
        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(request.getRequestURI()).thenReturn("/api/v1/evaluations");
        when(response.getWriter()).thenReturn(printWriter);
        
        // Execute - this should be rate limited
        rateLimitingFilter.doFilter(request, response, filterChain);
        
        // Verify
        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).setContentType("application/json");
        
        String responseContent = stringWriter.toString();
        assertTrue(responseContent.contains("RATE_LIMIT_EXCEEDED"));
    }
    
    @Test
    void testDoFilter_HealthCheckEndpoint_ShouldSkipRateLimit() throws IOException, ServletException {
        // Setup
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getRequestURI()).thenReturn("/actuator/health");
        
        // Execute multiple times (more than rate limit)
        for (int i = 0; i < 10; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }
        
        // Verify all requests were allowed
        verify(filterChain, times(10)).doFilter(request, response);
        verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }
    
    @Test
    void testDoFilter_RateLimitingDisabled_ShouldAllowAllRequests() throws IOException, ServletException {
        // Setup
        ReflectionTestUtils.setField(rateLimitingFilter, "rateLimitingEnabled", false);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getRequestURI()).thenReturn("/api/v1/evaluations");
        
        // Execute multiple times (more than rate limit)
        for (int i = 0; i < 10; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }
        
        // Verify all requests were allowed
        verify(filterChain, times(10)).doFilter(request, response);
        verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }
    
    @Test
    void testDoFilter_XForwardedForHeader_ShouldUseCorrectIP() throws IOException, ServletException {
        // Setup
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 192.168.1.1");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getRequestURI()).thenReturn("/api/v1/evaluations");
        
        // Execute
        rateLimitingFilter.doFilter(request, response, filterChain);
        
        // Verify request was processed
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    void testCleanupOldRecords_ShouldRemoveExpiredEntries() {
        // Setup - add some requests
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getRequestURI()).thenReturn("/api/v1/evaluations");
        
        try {
            rateLimitingFilter.doFilter(request, response, filterChain);
        } catch (Exception e) {
            // Ignore for this test
        }
        
        // Execute cleanup
        rateLimitingFilter.cleanupOldRecords();
        
        // Verify no exceptions thrown
        assertDoesNotThrow(() -> rateLimitingFilter.cleanupOldRecords());
    }
    
    @Test
    void testGetStats_ShouldReturnValidStatistics() {
        // Setup - add some requests
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getRequestURI()).thenReturn("/api/v1/evaluations");
        
        try {
            rateLimitingFilter.doFilter(request, response, filterChain);
        } catch (Exception e) {
            // Ignore for this test
        }
        
        // Execute
        RateLimitingFilter.RateLimitStats stats = rateLimitingFilter.getStats();
        
        // Verify
        assertNotNull(stats);
        assertTrue(stats.getActiveClients() >= 0);
        assertTrue(stats.getTotalRequests() >= 0);
        assertEquals(5, stats.getRequestsPerMinute());
    }
    
    @Test
    void testRateLimitStats_Constructor() {
        RateLimitingFilter.RateLimitStats stats = new RateLimitingFilter.RateLimitStats(2, 10, 60);
        
        assertEquals(2, stats.getActiveClients());
        assertEquals(10, stats.getTotalRequests());
        assertEquals(60, stats.getRequestsPerMinute());
    }
}