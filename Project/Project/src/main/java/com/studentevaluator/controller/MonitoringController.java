package com.studentevaluator.controller;

import com.studentevaluator.monitoring.PerformanceMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for monitoring and performance metrics
 */
@RestController
@RequestMapping("/api/v1/monitoring")
public class MonitoringController {

    private final PerformanceMonitor performanceMonitor;

    @Autowired
    public MonitoringController(PerformanceMonitor performanceMonitor) {
        this.performanceMonitor = performanceMonitor;
    }

    /**
     * Get current performance statistics
     */
    @GetMapping("/performance")
    public ResponseEntity<PerformanceMonitor.PerformanceStats> getPerformanceStats() {
        PerformanceMonitor.PerformanceStats stats = performanceMonitor.getPerformanceStats();
        return ResponseEntity.ok(stats);
    }
}