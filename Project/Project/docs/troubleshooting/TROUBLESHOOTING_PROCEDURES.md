# Student Evaluator System - Troubleshooting Guide

## Overview

This guide provides comprehensive troubleshooting information for the Student Evaluator System, covering common issues, diagnostic procedures, and resolution steps for both development and production environments.

## Quick Diagnostic Checklist

Before diving into specific issues, run through this quick checklist:

### System Health Check

```bash
# 1. Check application health
curl http://localhost:8080/actuator/health

# 2. Check database connectivity
curl http://localhost:8080/actuator/health/db

# 3. Check disk space
df -h

# 4. Check memory usage
free -h

# 5. Check Java process
jps -l | grep student-evaluator

# 6. Check application logs
tail -f logs/student-evaluator.log
```

### Expected Healthy Responses

**Health Endpoint:**
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

## Common Issues and Solutions

### 1. Application Startup Issues

#### Issue: Application fails to start with "Port already in use"

**Symptoms:**
```
***************************
APPLICATION FAILED TO START
***************************

Description:
Web server failed to start. Port 8080 was already in use.
```

**Diagnosis:**
```bash
# Find process using port 8080
lsof -i :8080
netstat -tulpn | grep :8080

# On Windows
netstat -ano | findstr :8080
```

**Solutions:**
```bash
# Option 1: Kill the process using the port
kill -9 <PID>

# Option 2: Use a different port
java -jar app.jar --server.port=8081

# Option 3: Set port in environment
export SERVER_PORT=8081
java -jar app.jar
```

#### Issue: Database connection failure on startup

**Symptoms:**
```
Caused by: java.sql.SQLException: Access denied for user 'evaluator'@'localhost'
```

**Diagnosis:**
```bash
# Test database connection manually
mysql -h localhost -u evaluator -p student_evaluator_dev

# Check database service status
sudo systemctl status mysql
docker ps | grep mysql
```

**Solutions:**
```bash
# 1. Verify database credentials
mysql -u root -p
CREATE USER 'evaluator'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON student_evaluator_dev.* TO 'evaluator'@'localhost';
FLUSH PRIVILEGES;

# 2. Check database URL in configuration
# Verify application.yml or environment variables

# 3. Restart database service
sudo systemctl restart mysql
docker restart student-evaluator-mysql
```

#### Issue: Out of Memory Error during startup

**Symptoms:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Diagnosis:**
```bash
# Check available memory
free -h

# Check current JVM settings
ps aux | grep java
```

**Solutions:**
```bash
# Increase heap size
java -Xms512m -Xmx2g -jar app.jar

# For Docker containers
docker run -e JAVA_OPTS="-Xms512m -Xmx2g" your-image

# Set in environment
export JAVA_OPTS="-Xms512m -Xmx2g"
```

### 2. Runtime Issues

#### Issue: High CPU usage

**Symptoms:**
- Application becomes slow
- High CPU utilization in monitoring tools
- Slow response times

**Diagnosis:**
```bash
# Check CPU usage
top -p <java-pid>
htop

# Get thread dump
jstack <java-pid> > thread-dump.txt

# Check for CPU-intensive threads
top -H -p <java-pid>
```

**Analysis:**
```bash
# Analyze thread dump
grep -A 10 "RUNNABLE" thread-dump.txt
grep -c "java.lang.Thread.State: BLOCKED" thread-dump.txt
```

**Solutions:**
```bash
# 1. Increase thread pool size if needed
# Check application.yml:
# server.tomcat.threads.max=200

# 2. Optimize database queries
# Enable SQL logging to identify slow queries

# 3. Add caching for frequently accessed data

# 4. Scale horizontally if needed
```

#### Issue: Memory leaks

**Symptoms:**
- Gradually increasing memory usage
- OutOfMemoryError after running for some time
- Frequent garbage collection

**Diagnosis:**
```bash
# Monitor memory usage over time
jstat -gc <java-pid> 5s

# Generate heap dump
jmap -dump:format=b,file=heap-dump.hprof <java-pid>

# Analyze heap dump with Eclipse MAT or VisualVM
```

**Common Causes and Solutions:**
```java
// 1. Unclosed resources
// Bad:
FileInputStream fis = new FileInputStream(file);
// process file
// fis never closed

// Good:
try (FileInputStream fis = new FileInputStream(file)) {
    // process file
} // automatically closed

// 2. Static collections growing indefinitely
// Bad:
private static Map<String, Object> cache = new HashMap<>();

// Good:
private static Map<String, Object> cache = new ConcurrentHashMap<>();
// Implement cache eviction policy

// 3. Event listeners not removed
// Bad:
eventPublisher.addEventListener(listener);
// listener never removed

// Good:
eventPublisher.addEventListener(listener);
// Later: eventPublisher.removeEventListener(listener);
```

#### Issue: Database connection pool exhaustion

**Symptoms:**
```
HikariPool-1 - Connection is not available, request timed out after 30000ms
```

**Diagnosis:**
```bash
# Check connection pool metrics
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
curl http://localhost:8080/actuator/metrics/hikaricp.connections.pending

# Check database processlist
mysql -u root -p -e "SHOW PROCESSLIST;"
```

**Solutions:**
```yaml
# 1. Increase pool size
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Increase from default 10
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000

# 2. Optimize queries to reduce connection hold time
# 3. Implement connection leak detection
spring:
  datasource:
    hikari:
      leak-detection-threshold: 60000  # 60 seconds
```

### 3. API Issues

#### Issue: 500 Internal Server Error

**Symptoms:**
```json
{
  "error": {
    "code": "INTERNAL_SERVER_ERROR",
    "message": "An unexpected error occurred",
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

**Diagnosis:**
```bash
# Check application logs
tail -f logs/student-evaluator.log | grep ERROR

# Check specific error details
grep -A 10 -B 5 "Internal Server Error" logs/student-evaluator.log

# Check stack traces
grep -A 20 "Exception" logs/student-evaluator.log
```

**Common Causes and Solutions:**

1. **NullPointerException:**
```java
// Add null checks and validation
@PostMapping("/evaluations")
public ResponseEntity<?> createEvaluation(@RequestBody EvaluationRequest request) {
    if (request == null || request.getStudentId() == null) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_REQUEST", "Student ID is required"));
    }
    // ... rest of method
}
```

2. **Database constraint violations:**
```java
// Handle database exceptions
@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
        DataIntegrityViolationException ex) {
    return ResponseEntity.badRequest()
        .body(new ErrorResponse("DATA_INTEGRITY_ERROR", 
                               "Data constraint violation: " + ex.getMessage()));
}
```

#### Issue: Request timeout

**Symptoms:**
- Requests taking longer than expected
- Gateway timeout errors (504)
- Client timeout errors

**Diagnosis:**
```bash
# Check response time metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# Monitor active requests
curl http://localhost:8080/actuator/metrics/tomcat.threads.busy

# Check for long-running operations
jstack <java-pid> | grep -A 5 "evaluation"
```

**Solutions:**
```yaml
# 1. Increase timeout settings
server:
  tomcat:
    connection-timeout: 60000  # 60 seconds
    keep-alive-timeout: 60000

# 2. Implement async processing
@Async
public CompletableFuture<EvaluationResult> evaluateAsync(EvaluationRequest request) {
    // Long-running evaluation logic
}

# 3. Add request timeout configuration
spring:
  mvc:
    async:
      request-timeout: 300000  # 5 minutes
```

#### Issue: File upload failures

**Symptoms:**
```json
{
  "error": {
    "code": "FILE_UPLOAD_FAILED",
    "message": "Failed to upload file",
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

**Diagnosis:**
```bash
# Check file upload limits
curl -I http://localhost:8080/actuator/configprops | grep multipart

# Check disk space
df -h /tmp

# Check file permissions
ls -la /tmp/evaluations/
```

**Solutions:**
```yaml
# 1. Increase file upload limits
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

# 2. Check temp directory permissions
server:
  tomcat:
    basedir: /tmp/tomcat
```

```bash
# 3. Ensure temp directory exists and is writable
mkdir -p /tmp/evaluations
chmod 755 /tmp/evaluations
```

### 4. Compilation and Testing Issues

#### Issue: Java compilation failures

**Symptoms:**
```json
{
  "compilationResult": {
    "successful": false,
    "errors": ["cannot find symbol: variable x"]
  }
}
```

**Diagnosis:**
```bash
# Check Java compiler availability
javac -version

# Check temp directory for compilation artifacts
ls -la /tmp/evaluations/

# Check compilation logs
grep "compilation" logs/student-evaluator.log
```

**Solutions:**
```java
// 1. Improve error handling in JavaCompiler
@Component
public class JavaCompiler {
    public CompilationResult compile(File sourceFile, String outputDir) {
        try {
            // Compilation logic
        } catch (Exception e) {
            log.error("Compilation failed for file: {}", sourceFile.getName(), e);
            return CompilationResult.failed(e.getMessage());
        }
    }
}

// 2. Add input validation
public ValidationResult validateJavaFile(MultipartFile file) {
    if (!file.getOriginalFilename().endsWith(".java")) {
        return ValidationResult.invalid("Only .java files are allowed");
    }
    // Additional validation
}
```

#### Issue: JUnit test execution failures

**Symptoms:**
```json
{
  "testResult": {
    "totalTests": 0,
    "passedTests": 0,
    "failedTests": 0,
    "executionLog": "Failed to load test class"
  }
}
```

**Diagnosis:**
```bash
# Check JUnit dependencies
mvn dependency:tree | grep junit

# Check test file compilation
ls -la /tmp/evaluations/*/test-classes/

# Check classpath issues
grep "ClassNotFoundException" logs/student-evaluator.log
```

**Solutions:**
```java
// 1. Improve test runner error handling
@Component
public class JUnitTestRunner {
    public TestExecutionResult runTests(File compiledClass, List<File> testFiles) {
        try {
            // Test execution logic
        } catch (ClassNotFoundException e) {
            log.error("Test class not found: {}", e.getMessage());
            return TestExecutionResult.failed("Test class not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Test execution failed", e);
            return TestExecutionResult.failed("Test execution failed: " + e.getMessage());
        }
    }
}
```

### 5. Performance Issues

#### Issue: Slow evaluation processing

**Symptoms:**
- Evaluations taking longer than expected
- Queue buildup
- Timeout errors

**Diagnosis:**
```bash
# Check evaluation metrics
curl http://localhost:8080/actuator/metrics/evaluations.duration

# Check thread pool status
curl http://localhost:8080/actuator/metrics/executor.active

# Monitor evaluation queue
curl http://localhost:8080/actuator/metrics/evaluations.queued
```

**Solutions:**
```yaml
# 1. Increase thread pool size
evaluation:
  async:
    core-pool-size: 10
    max-pool-size: 50
    queue-capacity: 100

# 2. Optimize compilation process
evaluation:
  compiler:
    cache-compiled-classes: true
    parallel-compilation: true

# 3. Implement evaluation timeout
evaluation:
  timeout: 60000  # 60 seconds
```

#### Issue: Database query performance

**Symptoms:**
- Slow API responses
- High database CPU usage
- Long-running queries

**Diagnosis:**
```sql
-- Enable slow query log
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;

-- Check slow queries
SELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 10;

-- Check current queries
SHOW PROCESSLIST;
```

**Solutions:**
```sql
-- 1. Add database indexes
CREATE INDEX idx_evaluations_student_id ON evaluations(student_id);
CREATE INDEX idx_evaluations_assignment_id ON evaluations(assignment_id);
CREATE INDEX idx_evaluations_status ON evaluations(status);
CREATE INDEX idx_evaluations_evaluated_at ON evaluations(evaluated_at);

-- 2. Optimize queries
-- Instead of:
SELECT * FROM evaluations WHERE student_id = 'student123';

-- Use:
SELECT evaluation_id, score, status, evaluated_at 
FROM evaluations 
WHERE student_id = 'student123' 
ORDER BY evaluated_at DESC 
LIMIT 10;
```

```java
// 3. Implement pagination
@Query("SELECT e FROM Evaluation e WHERE e.studentId = :studentId ORDER BY e.evaluatedAt DESC")
Page<Evaluation> findByStudentId(@Param("studentId") String studentId, Pageable pageable);
```

## Environment-Specific Issues

### Development Environment

#### Issue: Docker containers not starting

**Symptoms:**
```bash
docker-compose up -d
# Services fail to start
```

**Diagnosis:**
```bash
# Check container status
docker-compose ps

# Check container logs
docker-compose logs mysql
docker-compose logs dynamodb

# Check port conflicts
netstat -tulpn | grep 3306
netstat -tulpn | grep 8000
```

**Solutions:**
```bash
# 1. Stop conflicting services
sudo systemctl stop mysql
sudo service mysql stop

# 2. Use different ports
# Edit docker-compose.yml:
ports:
  - "3307:3306"  # Use port 3307 instead of 3306

# 3. Clean up Docker resources
docker-compose down -v
docker system prune -f
```

#### Issue: Hot reload not working

**Symptoms:**
- Code changes not reflected without restart
- DevTools not triggering restart

**Diagnosis:**
```bash
# Check if DevTools is included
mvn dependency:tree | grep devtools

# Check IDE build settings
# IntelliJ: Build project automatically should be enabled
# VS Code: Java extension should auto-compile
```

**Solutions:**
```xml
<!-- Ensure DevTools is in pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

```yaml
# Enable in application-dev.yml
spring:
  devtools:
    restart:
      enabled: true
    livereload:
      enabled: true
```

### Production Environment

#### Issue: High memory usage in production

**Symptoms:**
- Containers being killed by OOM killer
- High memory alerts
- Application restarts

**Diagnosis:**
```bash
# Check container memory limits
kubectl describe pod <pod-name>

# Check JVM memory usage
kubectl exec <pod-name> -- jstat -gc 1

# Get heap dump
kubectl exec <pod-name> -- jmap -dump:format=b,file=/tmp/heap.hprof 1
```

**Solutions:**
```yaml
# 1. Adjust container resources
resources:
  requests:
    memory: "1Gi"
  limits:
    memory: "2Gi"

# 2. Optimize JVM settings
env:
  - name: JAVA_OPTS
    value: "-Xms1g -Xmx1.5g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 3. Implement memory monitoring
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

#### Issue: Load balancer health check failures

**Symptoms:**
- Pods marked as unhealthy
- Traffic not routing to pods
- 503 Service Unavailable errors

**Diagnosis:**
```bash
# Check pod health
kubectl get pods -o wide

# Check health endpoint
kubectl exec <pod-name> -- curl http://localhost:8080/actuator/health

# Check readiness probe
kubectl describe pod <pod-name> | grep -A 10 "Readiness"
```

**Solutions:**
```yaml
# 1. Adjust probe settings
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3

livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 30
  timeoutSeconds: 10
  failureThreshold: 3
```

## Monitoring and Alerting Issues

### Issue: Missing metrics

**Symptoms:**
- Metrics not appearing in monitoring dashboard
- Prometheus not scraping metrics
- Missing application insights

**Diagnosis:**
```bash
# Check metrics endpoint
curl http://localhost:8080/actuator/metrics

# Check Prometheus endpoint
curl http://localhost:8080/actuator/prometheus

# Check Prometheus configuration
kubectl get configmap prometheus-config -o yaml
```

**Solutions:**
```yaml
# 1. Enable metrics in application
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true

# 2. Configure Prometheus scraping
scrape_configs:
  - job_name: 'student-evaluator'
    kubernetes_sd_configs:
      - role: pod
    relabel_configs:
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
        action: keep
        regex: true
```

### Issue: Alert fatigue

**Symptoms:**
- Too many alerts firing
- Important alerts getting lost
- Alert noise

**Solutions:**
```yaml
# 1. Implement alert severity levels
groups:
  - name: student-evaluator-critical
    rules:
      - alert: ServiceDown
        expr: up{job="student-evaluator"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Student Evaluator service is down"

  - name: student-evaluator-warning
    rules:
      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"

# 2. Implement alert grouping and inhibition
route:
  group_by: ['alertname', 'cluster', 'service']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'web.hook'

inhibit_rules:
  - source_match:
      severity: 'critical'
    target_match:
      severity: 'warning'
    equal: ['alertname', 'cluster', 'service']
```

## Log Analysis and Debugging

### Structured Log Analysis

**Common Log Patterns:**
```bash
# Find errors in logs
grep -i error logs/student-evaluator.log

# Find specific evaluation issues
grep "evaluation.*failed" logs/student-evaluator.log

# Find database connection issues
grep -i "connection" logs/student-evaluator.log | grep -i "fail\|error\|timeout"

# Find memory issues
grep -i "outofmemory\|heap\|gc" logs/student-evaluator.log

# Find compilation issues
grep "compilation.*failed" logs/student-evaluator.log
```

**Log Analysis with ELK Stack:**
```json
// Kibana query for errors
{
  "query": {
    "bool": {
      "must": [
        {"match": {"level": "ERROR"}},
        {"range": {"@timestamp": {"gte": "now-1h"}}}
      ]
    }
  }
}

// Query for specific evaluation failures
{
  "query": {
    "bool": {
      "must": [
        {"match": {"logger": "com.studentevaluator.service.EvaluationService"}},
        {"match": {"message": "evaluation failed"}}
      ]
    }
  }
}
```

### Debug Mode Configuration

**Enable Debug Logging:**
```yaml
# application-debug.yml
logging:
  level:
    com.studentevaluator: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
```

**Remote Debugging:**
```bash
# Start application with debug port
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar app.jar

# Connect IDE debugger to localhost:5005
```

## Recovery Procedures

### Database Recovery

**MySQL Recovery:**
```bash
# 1. Stop application
kubectl scale deployment student-evaluator --replicas=0

# 2. Create database backup
mysqldump -h $DB_HOST -u $DB_USER -p$DB_PASSWORD student_evaluator > backup.sql

# 3. Restore from backup if needed
mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD student_evaluator < backup.sql

# 4. Restart application
kubectl scale deployment student-evaluator --replicas=3
```

**DynamoDB Recovery:**
```bash
# 1. Restore from point-in-time recovery
aws dynamodb restore-table-from-backup \
    --target-table-name evaluation-logs-restored \
    --backup-arn arn:aws:dynamodb:region:account:table/evaluation-logs/backup/backup-id

# 2. Update application configuration to use restored table
kubectl set env deployment/student-evaluator DYNAMODB_TABLE=evaluation-logs-restored
```

### Application Recovery

**Rolling Restart:**
```bash
# Kubernetes
kubectl rollout restart deployment/student-evaluator

# ECS
aws ecs update-service --cluster cluster-name --service service-name --force-new-deployment
```

**Rollback to Previous Version:**
```bash
# Kubernetes
kubectl rollout undo deployment/student-evaluator

# ECS - update to previous task definition
aws ecs update-service --cluster cluster-name --service service-name --task-definition previous-task-def
```

## Prevention Strategies

### Proactive Monitoring

1. **Set up comprehensive health checks**
2. **Implement circuit breakers for external dependencies**
3. **Monitor key business metrics**
4. **Set up automated alerting**
5. **Regular performance testing**

### Code Quality

1. **Implement comprehensive testing**
2. **Use static code analysis tools**
3. **Regular security scans**
4. **Code reviews for all changes**
5. **Documentation updates**

### Infrastructure

1. **Regular backup testing**
2. **Disaster recovery drills**
3. **Capacity planning**
4. **Security updates**
5. **Performance baseline monitoring**

## Getting Help

### Internal Resources

1. **Check application logs first**
2. **Review monitoring dashboards**
3. **Consult this troubleshooting guide**
4. **Check system documentation**

### External Resources

1. **Spring Boot Documentation**: https://spring.io/projects/spring-boot
2. **MySQL Documentation**: https://dev.mysql.com/doc/
3. **AWS Documentation**: https://docs.aws.amazon.com/
4. **Kubernetes Documentation**: https://kubernetes.io/docs/

### Escalation Process

1. **Level 1**: Check logs and basic diagnostics
2. **Level 2**: Deep dive analysis and advanced diagnostics
3. **Level 3**: Vendor support or expert consultation

Remember to always gather relevant logs, metrics, and system information before escalating issues.