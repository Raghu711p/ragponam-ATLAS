package com.studentevaluator.monitoring;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Test class for PerformanceMonitor
 */
class PerformanceMonitorTest {

    private PerformanceMonitor performanceMonitor;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        performanceMonitor = new PerformanceMonitor(meterRegistry);
    }

    @Test
    @DisplayName("Should record evaluation time successfully")
    void testRecordEvaluationTime() {
        // Given
        Duration duration = Duration.ofMillis(1500);
        boolean success = true;

        // When
        performanceMonitor.recordEvaluationTime(duration, success);

        // Then
        PerformanceMonitor.PerformanceStats stats = performanceMonitor.getPerformanceStats();
        assertEquals(1, stats.getTotalEvaluations());
        assertEquals(1, stats.getSuccessfulEvaluations());
        assertEquals(0, stats.getFailedEvaluations());
        assertTrue(stats.getAverageEvaluationTime() > 0);
    }

    @Test
    @DisplayName("Should record compilation time successfully")
    void testRecordCompilationTime() {
        // Given
        Duration duration = Duration.ofMillis(800);
        boolean success = true;

        // When
        performanceMonitor.recordCompilationTime(duration, success);

        // Then
        PerformanceMonitor.PerformanceStats stats = performanceMonitor.getPerformanceStats();
        assertEquals(0, stats.getCompilationFailures());
        assertTrue(stats.getAverageCompilationTime() > 0);
    }

    @Test
    @DisplayName("Should record test execution time successfully")
    void testRecordTestExecutionTime() {
        // Given
        Duration duration = Duration.ofMillis(1200);
        boolean success = true;

        // When
        performanceMonitor.recordTestExecutionTime(duration, success);

        // Then
        PerformanceMonitor.PerformanceStats stats = performanceMonitor.getPerformanceStats();
        assertEquals(0, stats.getTestFailures());
        assertTrue(stats.getAverageTestTime() > 0);
    }

    @Test
    @DisplayName("Should calculate success rate correctly")
    void testSuccessRateCalculation() {
        // Given
        performanceMonitor.recordEvaluationTime(Duration.ofMillis(1000), true);
        performanceMonitor.recordEvaluationTime(Duration.ofMillis(1500), true);
        performanceMonitor.recordEvaluationTime(Duration.ofMillis(2000), false);

        // When
        PerformanceMonitor.PerformanceStats stats = performanceMonitor.getPerformanceStats();

        // Then
        assertEquals(3, stats.getTotalEvaluations());
        assertEquals(2, stats.getSuccessfulEvaluations());
        assertEquals(1, stats.getFailedEvaluations());
        assertEquals(66.67, stats.getSuccessRate(), 0.01);
    }

    @Test
    @DisplayName("Should handle zero evaluations gracefully")
    void testZeroEvaluations() {
        // When
        PerformanceMonitor.PerformanceStats stats = performanceMonitor.getPerformanceStats();

        // Then
        assertEquals(0, stats.getTotalEvaluations());
        assertEquals(0, stats.getSuccessRate());
    }
}