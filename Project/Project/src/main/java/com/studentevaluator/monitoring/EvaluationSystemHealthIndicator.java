package com.studentevaluator.monitoring;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.studentevaluator.service.EvaluationService;

/**
 * Custom health indicator for the evaluation system
 */
@Component
public class EvaluationSystemHealthIndicator {

    private final EvaluationService evaluationService;
    private final String tempDirectory;

    public EvaluationSystemHealthIndicator(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
        this.tempDirectory = System.getProperty("java.io.tmpdir") + "/evaluations";
    }

    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        try {
            health.put("status", "UP");
            
            // Check temp directory accessibility
            checkTempDirectory(health);
            
            // Check Java compiler availability
            checkJavaCompiler(health);
            
            // Check system resources
            checkSystemResources(health);
            
            return health;
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return health;
        }
    }

    private void checkTempDirectory(Map<String, Object> health) {
        try {
            Path tempPath = Paths.get(tempDirectory);
            if (!Files.exists(tempPath)) {
                Files.createDirectories(tempPath);
            }
            
            // Test write access
            File testFile = new File(tempPath.toFile(), "health-check.tmp");
            if (testFile.createNewFile()) {
                testFile.delete();
                health.put("tempDirectory", "accessible");
            } else {
                health.put("tempDirectory", "write-protected");
            }
        } catch (Exception e) {
            health.put("tempDirectory", "error: " + e.getMessage());
        }
    }

    private void checkJavaCompiler(Map<String, Object> health) {
        try {
            javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
            if (compiler != null) {
                health.put("javaCompiler", "available");
            } else {
                health.put("javaCompiler", "not available");
            }
        } catch (Exception e) {
            health.put("javaCompiler", "error: " + e.getMessage());
        }
    }

    private void checkSystemResources(Map<String, Object> health) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        health.put("memory.max", formatBytes(maxMemory));
        health.put("memory.total", formatBytes(totalMemory));
        health.put("memory.used", formatBytes(usedMemory));
        health.put("memory.free", formatBytes(freeMemory));
        health.put("memory.usage.percent", String.format("%.2f%%", memoryUsagePercent));
        
        // Check available processors
        health.put("processors", runtime.availableProcessors());
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}