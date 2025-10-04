package com.studentevaluator.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Performance monitoring component for tracking evaluation processing times and metrics
 */
@Component
public class PerformanceMonitor {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);

    private final Timer evaluationTimer;
    private final Timer compilationTimer;
    private final Timer testExecutionTimer;
    private final Counter evaluationSuccessCounter;
    private final Counter evaluationFailureCounter;
    private final Counter compilationFailureCounter;
    private final Counter testFailureCounter;

    // Performance thresholds (in milliseconds)
    private static final long EVALUATION_WARNING_THRESHOLD = 30000; // 30 seconds
    private static final long EVALUATION_CRITICAL_THRESHOLD = 60000; // 60 seconds
    private static final long COMPILATION_WARNING_THRESHOLD = 10000; // 10 seconds
    private static final long TEST_WARNING_THRESHOLD = 20000; // 20 seconds

    public PerformanceMonitor(MeterRegistry meterRegistry) {
        this.evaluationTimer = Timer.builder("evaluation.duration")
                .description("Time taken for complete evaluation process")
                .register(meterRegistry);

        this.compilationTimer = Timer.builder("compilation.duration")
                .description("Time taken for Java compilation")
                .register(meterRegistry);

        this.testExecutionTimer = Timer.builder("test.execution.duration")
                .description("Time taken for test execution")
                .register(meterRegistry);

        this.evaluationSuccessCounter = Counter.builder("evaluation.success")
                .description("Number of successful evaluations")
                .register(meterRegistry);

        this.evaluationFailureCounter = Counter.builder("evaluation.failure")
                .description("Number of failed evaluations")
                .register(meterRegistry);

        this.compilationFailureCounter = Counter.builder("compilation.failure")
                .description("Number of compilation failures")
                .register(meterRegistry);

        this.testFailureCounter = Counter.builder("test.failure")
                .description("Number of test execution failures")
                .register(meterRegistry);
    }

    /**
     * Records evaluation processing time and checks for performance issues
     */
    public void recordEvaluationTime(Duration duration, boolean success) {
        evaluationTimer.record(duration);
        
        if (success) {
            evaluationSuccessCounter.increment();
        } else {
            evaluationFailureCounter.increment();
        }

        long durationMs = duration.toMillis();
        
        if (durationMs > EVALUATION_CRITICAL_THRESHOLD) {
            logger.error("CRITICAL: Evaluation took {} ms, exceeding critical threshold of {} ms", 
                        durationMs, EVALUATION_CRITICAL_THRESHOLD);
        } else if (durationMs > EVALUATION_WARNING_THRESHOLD) {
            logger.warn("WARNING: Evaluation took {} ms, exceeding warning threshold of {} ms", 
                       durationMs, EVALUATION_WARNING_THRESHOLD);
        }
    }

    /**
     * Records compilation time and checks for performance issues
     */
    public void recordCompilationTime(Duration duration, boolean success) {
        compilationTimer.record(duration);
        
        if (!success) {
            compilationFailureCounter.increment();
        }

        long durationMs = duration.toMillis();
        
        if (durationMs > COMPILATION_WARNING_THRESHOLD) {
            logger.warn("WARNING: Compilation took {} ms, exceeding warning threshold of {} ms", 
                       durationMs, COMPILATION_WARNING_THRESHOLD);
        }
    }

    /**
     * Records test execution time and checks for performance issues
     */
    public void recordTestExecutionTime(Duration duration, boolean success) {
        testExecutionTimer.record(duration);
        
        if (!success) {
            testFailureCounter.increment();
        }

        long durationMs = duration.toMillis();
        
        if (durationMs > TEST_WARNING_THRESHOLD) {
            logger.warn("WARNING: Test execution took {} ms, exceeding warning threshold of {} ms", 
                       durationMs, TEST_WARNING_THRESHOLD);
        }
    }

    /**
     * Gets current performance statistics
     */
    public PerformanceStats getPerformanceStats() {
        return new PerformanceStats(
                evaluationTimer.count(),
                evaluationTimer.mean(TimeUnit.MILLISECONDS),
                evaluationTimer.max(TimeUnit.MILLISECONDS),
                compilationTimer.mean(TimeUnit.MILLISECONDS),
                testExecutionTimer.mean(TimeUnit.MILLISECONDS),
                evaluationSuccessCounter.count(),
                evaluationFailureCounter.count(),
                compilationFailureCounter.count(),
                testFailureCounter.count()
        );
    }

    /**
     * Performance statistics data class
     */
    public static class PerformanceStats {
        private final long totalEvaluations;
        private final double averageEvaluationTime;
        private final double maxEvaluationTime;
        private final double averageCompilationTime;
        private final double averageTestTime;
        private final double successfulEvaluations;
        private final double failedEvaluations;
        private final double compilationFailures;
        private final double testFailures;

        public PerformanceStats(long totalEvaluations, double averageEvaluationTime, 
                              double maxEvaluationTime, double averageCompilationTime, 
                              double averageTestTime, double successfulEvaluations, 
                              double failedEvaluations, double compilationFailures, 
                              double testFailures) {
            this.totalEvaluations = totalEvaluations;
            this.averageEvaluationTime = averageEvaluationTime;
            this.maxEvaluationTime = maxEvaluationTime;
            this.averageCompilationTime = averageCompilationTime;
            this.averageTestTime = averageTestTime;
            this.successfulEvaluations = successfulEvaluations;
            this.failedEvaluations = failedEvaluations;
            this.compilationFailures = compilationFailures;
            this.testFailures = testFailures;
        }

        // Getters
        public long getTotalEvaluations() { return totalEvaluations; }
        public double getAverageEvaluationTime() { return averageEvaluationTime; }
        public double getMaxEvaluationTime() { return maxEvaluationTime; }
        public double getAverageCompilationTime() { return averageCompilationTime; }
        public double getAverageTestTime() { return averageTestTime; }
        public double getSuccessfulEvaluations() { return successfulEvaluations; }
        public double getFailedEvaluations() { return failedEvaluations; }
        public double getCompilationFailures() { return compilationFailures; }
        public double getTestFailures() { return testFailures; }
        
        public double getSuccessRate() {
            return totalEvaluations > 0 ? (successfulEvaluations / totalEvaluations) * 100 : 0;
        }

        @Override
        public String toString() {
            return String.format(
                "PerformanceStats{total=%d, avgTime=%.2fms, maxTime=%.2fms, successRate=%.2f%%}",
                totalEvaluations, averageEvaluationTime, maxEvaluationTime, getSuccessRate()
            );
        }
    }
}