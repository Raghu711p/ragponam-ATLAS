# Student Evaluator System - Production Deployment Guide

## Overview

This guide provides comprehensive instructions for deploying the Student Evaluator System to production environments. It covers infrastructure setup, security considerations, monitoring, and operational procedures.

## Architecture Overview

### Production Architecture

```mermaid
graph TB
    subgraph "Load Balancer"
        LB[Application Load Balancer]
    end
    
    subgraph "Application Tier"
        APP1[App Instance 1]
        APP2[App Instance 2]
        APP3[App Instance 3]
    end
    
    subgraph "Database Tier"
        RDS[(RDS MySQL)]
        DYNAMO[(DynamoDB)]
        REDIS[(Redis Cache)]
    end
    
    subgraph "Storage Tier"
        S3[S3 Bucket]
        EFS[EFS Storage]
    end
    
    subgraph "Monitoring"
        CW[CloudWatch]
        PROM[Prometheus]
        GRAF[Grafana]
    end
    
    LB --> APP1
    LB --> APP2
    LB --> APP3
    
    APP1 --> RDS
    APP2 --> RDS
    APP3 --> RDS
    
    APP1 --> DYNAMO
    APP2 --> DYNAMO
    APP3 --> DYNAMO
    
    APP1 --> REDIS
    APP2 --> REDIS
    APP3 --> REDIS
    
    APP1 --> S3
    APP2 --> S3
    APP3 --> S3
    
    APP1 --> EFS
    APP2 --> EFS
    APP3 --> EFS
    
    APP1 --> CW
    APP2 --> CW
    APP3 --> CW
```

## Prerequisites

### Infrastructure Requirements

1. **Compute Resources**
   - Minimum: 2 vCPUs, 4GB RAM per instance
   - Recommended: 4 vCPUs, 8GB RAM per instance
   - Auto-scaling group with 2-10 instances

2. **Database Requirements**
   - MySQL 8.0 or higher (RDS recommended)
   - DynamoDB with on-demand billing
   - Redis for caching (ElastiCache recommended)

3. **Storage Requirements**
   - S3 bucket for file storage
   - EFS for shared temporary files
   - Minimum 100GB storage per instance

4. **Network Requirements**
   - VPC with public and private subnets
   - Application Load Balancer
   - Security groups and NACLs
   - SSL/TLS certificates

### Software Requirements

1. **Container Runtime**
   - Docker 20.10 or higher
   - Kubernetes 1.21 or higher (if using K8s)

2. **Monitoring and Logging**
   - CloudWatch or equivalent
   - Prometheus and Grafana
   - ELK Stack or equivalent

3. **CI/CD Pipeline**
   - Jenkins, GitLab CI, or GitHub Actions
   - Container registry (ECR, Docker Hub)

## Deployment Options

### Option 1: AWS ECS with Fargate (Recommended)

#### 1. Infrastructure Setup

**VPC and Networking:**
```bash
# Create VPC
aws ec2 create-vpc --cidr-block 10.0.0.0/16 --tag-specifications 'ResourceType=vpc,Tags=[{Key=Name,Value=student-evaluator-vpc}]'

# Create subnets
aws ec2 create-subnet --vpc-id vpc-xxx --cidr-block 10.0.1.0/24 --availability-zone us-east-1a
aws ec2 create-subnet --vpc-id vpc-xxx --cidr-block 10.0.2.0/24 --availability-zone us-east-1b

# Create internet gateway
aws ec2 create-internet-gateway --tag-specifications 'ResourceType=internet-gateway,Tags=[{Key=Name,Value=student-evaluator-igw}]'
```

**RDS Database:**
```bash
# Create RDS subnet group
aws rds create-db-subnet-group \
    --db-subnet-group-name student-evaluator-subnet-group \
    --db-subnet-group-description "Subnet group for Student Evaluator" \
    --subnet-ids subnet-xxx subnet-yyy

# Create RDS instance
aws rds create-db-instance \
    --db-instance-identifier student-evaluator-db \
    --db-instance-class db.t3.medium \
    --engine mysql \
    --engine-version 8.0.35 \
    --master-username evaluator \
    --master-user-password SecurePassword123! \
    --allocated-storage 100 \
    --storage-type gp2 \
    --db-subnet-group-name student-evaluator-subnet-group \
    --vpc-security-group-ids sg-xxx \
    --backup-retention-period 7 \
    --multi-az \
    --storage-encrypted
```

**DynamoDB Table:**
```bash
# Create DynamoDB table
aws dynamodb create-table \
    --table-name evaluation-logs-prod \
    --attribute-definitions \
        AttributeName=evaluation_id,AttributeType=S \
        AttributeName=timestamp,AttributeType=S \
    --key-schema \
        AttributeName=evaluation_id,KeyType=HASH \
        AttributeName=timestamp,KeyType=RANGE \
    --billing-mode PAY_PER_REQUEST \
    --tags Key=Environment,Value=production Key=Application,Value=student-evaluator
```

**ElastiCache Redis:**
```bash
# Create ElastiCache subnet group
aws elasticache create-cache-subnet-group \
    --cache-subnet-group-name student-evaluator-cache-subnet \
    --cache-subnet-group-description "Cache subnet group for Student Evaluator" \
    --subnet-ids subnet-xxx subnet-yyy

# Create Redis cluster
aws elasticache create-replication-group \
    --replication-group-id student-evaluator-redis \
    --description "Redis cluster for Student Evaluator" \
    --node-type cache.t3.micro \
    --cache-subnet-group-name student-evaluator-cache-subnet \
    --security-group-ids sg-xxx \
    --num-cache-clusters 2 \
    --automatic-failover-enabled
```

#### 2. Container Configuration

**Dockerfile (Production):**
```dockerfile
FROM openjdk:11-jre-slim

# Create non-root user
RUN groupadd -r evaluator && useradd -r -g evaluator evaluator

# Install required packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy application jar
COPY target/student-evaluator-*.jar app.jar

# Create directories
RUN mkdir -p /app/logs /app/temp && \
    chown -R evaluator:evaluator /app

# Switch to non-root user
USER evaluator

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM options for production
ENV JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

**ECS Task Definition:**
```json
{
  "family": "student-evaluator-task",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "executionRoleArn": "arn:aws:iam::account:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::account:role/ecsTaskRole",
  "containerDefinitions": [
    {
      "name": "student-evaluator",
      "image": "your-account.dkr.ecr.us-east-1.amazonaws.com/student-evaluator:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        },
        {
          "name": "DB_HOST",
          "value": "student-evaluator-db.cluster-xxx.us-east-1.rds.amazonaws.com"
        },
        {
          "name": "REDIS_HOST",
          "value": "student-evaluator-redis.xxx.cache.amazonaws.com"
        }
      ],
      "secrets": [
        {
          "name": "DB_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:account:secret:student-evaluator/db-password"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/student-evaluator",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": [
          "CMD-SHELL",
          "curl -f http://localhost:8080/actuator/health || exit 1"
        ],
        "interval": 30,
        "timeout": 10,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

**ECS Service Configuration:**
```json
{
  "serviceName": "student-evaluator-service",
  "cluster": "student-evaluator-cluster",
  "taskDefinition": "student-evaluator-task:1",
  "desiredCount": 3,
  "launchType": "FARGATE",
  "networkConfiguration": {
    "awsvpcConfiguration": {
      "subnets": ["subnet-xxx", "subnet-yyy"],
      "securityGroups": ["sg-xxx"],
      "assignPublicIp": "DISABLED"
    }
  },
  "loadBalancers": [
    {
      "targetGroupArn": "arn:aws:elasticloadbalancing:us-east-1:account:targetgroup/student-evaluator-tg",
      "containerName": "student-evaluator",
      "containerPort": 8080
    }
  ],
  "healthCheckGracePeriodSeconds": 120,
  "deploymentConfiguration": {
    "maximumPercent": 200,
    "minimumHealthyPercent": 50,
    "deploymentCircuitBreaker": {
      "enable": true,
      "rollback": true
    }
  }
}
```

### Option 2: Kubernetes Deployment

#### 1. Kubernetes Manifests

**Namespace:**
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: student-evaluator
  labels:
    name: student-evaluator
```

**ConfigMap:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: student-evaluator-config
  namespace: student-evaluator
data:
  application.yml: |
    spring:
      profiles:
        active: prod
      datasource:
        url: jdbc:mysql://mysql-service:3306/student_evaluator
        username: evaluator
      jpa:
        hibernate:
          ddl-auto: validate
    
    aws:
      region: us-east-1
      dynamodb:
        table-name: evaluation-logs-prod
    
    server:
      port: 8080
    
    management:
      endpoints:
        web:
          exposure:
            include: health,metrics,prometheus
      endpoint:
        health:
          show-details: always
```

**Secret:**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: student-evaluator-secrets
  namespace: student-evaluator
type: Opaque
data:
  db-password: <base64-encoded-password>
  aws-access-key-id: <base64-encoded-key>
  aws-secret-access-key: <base64-encoded-secret>
```

**Deployment:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: student-evaluator
  namespace: student-evaluator
  labels:
    app: student-evaluator
spec:
  replicas: 3
  selector:
    matchLabels:
      app: student-evaluator
  template:
    metadata:
      labels:
        app: student-evaluator
    spec:
      containers:
      - name: student-evaluator
        image: your-registry/student-evaluator:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_HOST
          value: "mysql-service"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: student-evaluator-secrets
              key: db-password
        - name: AWS_ACCESS_KEY_ID
          valueFrom:
            secretKeyRef:
              name: student-evaluator-secrets
              key: aws-access-key-id
        - name: AWS_SECRET_ACCESS_KEY
          valueFrom:
            secretKeyRef:
              name: student-evaluator-secrets
              key: aws-secret-access-key
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        - name: temp-storage
          mountPath: /app/temp
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
      volumes:
      - name: config-volume
        configMap:
          name: student-evaluator-config
      - name: temp-storage
        emptyDir: {}
      imagePullSecrets:
      - name: registry-secret
```

**Service:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: student-evaluator-service
  namespace: student-evaluator
  labels:
    app: student-evaluator
spec:
  selector:
    app: student-evaluator
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP
```

**Ingress:**
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: student-evaluator-ingress
  namespace: student-evaluator
  annotations:
    kubernetes.io/ingress.class: "nginx"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/proxy-body-size: "10m"
spec:
  tls:
  - hosts:
    - api.studentevaluator.com
    secretName: student-evaluator-tls
  rules:
  - host: api.studentevaluator.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: student-evaluator-service
            port:
              number: 80
```

**HorizontalPodAutoscaler:**
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: student-evaluator-hpa
  namespace: student-evaluator
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: student-evaluator
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

## Configuration Management

### 1. Environment-Specific Configuration

**Production Configuration (`application-prod.yml`):**
```yaml
spring:
  profiles:
    active: prod
  
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT:3306}/${DB_NAME:student_evaluator}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

aws:
  region: ${AWS_REGION:us-east-1}
  dynamodb:
    table-name: ${DYNAMODB_TABLE:evaluation-logs-prod}
  s3:
    bucket-name: ${S3_BUCKET:student-evaluator-files-prod}

server:
  port: 8080
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain
  http2:
    enabled: true

evaluation:
  timeout: 60000
  max-file-size: 2097152
  temp-directory: ${TEMP_DIR:/app/temp}
  max-concurrent-evaluations: 50

security:
  rate-limit:
    requests-per-minute: 1000
    enabled: true
  cors:
    allowed-origins: ${CORS_ORIGINS:https://studentevaluator.com}
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: "*"
    allow-credentials: true

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: student-evaluator
      environment: production

logging:
  level:
    com.studentevaluator: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
  file:
    name: /app/logs/student-evaluator.log
    max-size: 100MB
    max-history: 30
```

### 2. Secrets Management

**AWS Secrets Manager:**
```bash
# Create database password secret
aws secretsmanager create-secret \
    --name "student-evaluator/db-password" \
    --description "Database password for Student Evaluator" \
    --secret-string "SecurePassword123!"

# Create AWS credentials secret
aws secretsmanager create-secret \
    --name "student-evaluator/aws-credentials" \
    --description "AWS credentials for Student Evaluator" \
    --secret-string '{"access_key_id":"AKIAIOSFODNN7EXAMPLE","secret_access_key":"wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"}'

# Create Redis password secret
aws secretsmanager create-secret \
    --name "student-evaluator/redis-password" \
    --description "Redis password for Student Evaluator" \
    --secret-string "RedisSecurePassword123!"
```

**Kubernetes Secrets:**
```bash
# Create secrets from files
kubectl create secret generic student-evaluator-secrets \
    --from-file=db-password=./secrets/db-password.txt \
    --from-file=aws-access-key-id=./secrets/aws-access-key-id.txt \
    --from-file=aws-secret-access-key=./secrets/aws-secret-access-key.txt \
    --namespace=student-evaluator

# Create TLS secret for ingress
kubectl create secret tls student-evaluator-tls \
    --cert=./certs/tls.crt \
    --key=./certs/tls.key \
    --namespace=student-evaluator
```

## Security Configuration

### 1. Network Security

**Security Groups (AWS):**
```bash
# Create security group for application
aws ec2 create-security-group \
    --group-name student-evaluator-app-sg \
    --description "Security group for Student Evaluator application"

# Allow HTTP/HTTPS from load balancer
aws ec2 authorize-security-group-ingress \
    --group-id sg-xxx \
    --protocol tcp \
    --port 8080 \
    --source-group sg-load-balancer

# Create security group for database
aws ec2 create-security-group \
    --group-name student-evaluator-db-sg \
    --description "Security group for Student Evaluator database"

# Allow MySQL from application
aws ec2 authorize-security-group-ingress \
    --group-id sg-yyy \
    --protocol tcp \
    --port 3306 \
    --source-group sg-xxx
```

**Network Policies (Kubernetes):**
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: student-evaluator-network-policy
  namespace: student-evaluator
spec:
  podSelector:
    matchLabels:
      app: student-evaluator
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: kube-system
    ports:
    - protocol: TCP
      port: 53
    - protocol: UDP
      port: 53
  - to: []
    ports:
    - protocol: TCP
      port: 3306
    - protocol: TCP
      port: 6379
    - protocol: TCP
      port: 443
```

### 2. Application Security

**Security Configuration:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/actuator/metrics").hasRole("ADMIN")
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().denyAll()
            )
            .oauth2ResourceServer().jwt();
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(corsAllowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### 3. SSL/TLS Configuration

**Application Load Balancer SSL:**
```bash
# Request SSL certificate
aws acm request-certificate \
    --domain-name api.studentevaluator.com \
    --validation-method DNS \
    --subject-alternative-names "*.studentevaluator.com"

# Create HTTPS listener
aws elbv2 create-listener \
    --load-balancer-arn arn:aws:elasticloadbalancing:us-east-1:account:loadbalancer/app/student-evaluator-alb \
    --protocol HTTPS \
    --port 443 \
    --certificates CertificateArn=arn:aws:acm:us-east-1:account:certificate/certificate-id \
    --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:us-east-1:account:targetgroup/student-evaluator-tg
```

## Monitoring and Logging

### 1. Application Monitoring

**CloudWatch Configuration:**
```yaml
# CloudWatch agent configuration
{
  "metrics": {
    "namespace": "StudentEvaluator/Application",
    "metrics_collected": {
      "cpu": {
        "measurement": ["cpu_usage_idle", "cpu_usage_iowait", "cpu_usage_user", "cpu_usage_system"],
        "metrics_collection_interval": 60
      },
      "disk": {
        "measurement": ["used_percent"],
        "metrics_collection_interval": 60,
        "resources": ["*"]
      },
      "mem": {
        "measurement": ["mem_used_percent"],
        "metrics_collection_interval": 60
      }
    }
  },
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [
          {
            "file_path": "/app/logs/student-evaluator.log",
            "log_group_name": "/aws/ecs/student-evaluator",
            "log_stream_name": "{instance_id}",
            "timezone": "UTC"
          }
        ]
      }
    }
  }
}
```

**Prometheus Configuration:**
```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "student-evaluator-rules.yml"

scrape_configs:
  - job_name: 'student-evaluator'
    static_configs:
      - targets: ['student-evaluator-service:80']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093
```

**Grafana Dashboard:**
```json
{
  "dashboard": {
    "title": "Student Evaluator System",
    "panels": [
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count[5m])",
            "legendFormat": "{{method}} {{uri}}"
          }
        ]
      },
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))",
            "legendFormat": "95th percentile"
          }
        ]
      },
      {
        "title": "Error Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count{status=~\"4..|5..\"}[5m])",
            "legendFormat": "Error rate"
          }
        ]
      },
      {
        "title": "JVM Memory Usage",
        "type": "graph",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{area=\"heap\"}",
            "legendFormat": "Heap used"
          },
          {
            "expr": "jvm_memory_max_bytes{area=\"heap\"}",
            "legendFormat": "Heap max"
          }
        ]
      }
    ]
  }
}
```

### 2. Alerting Configuration

**CloudWatch Alarms:**
```bash
# High CPU utilization alarm
aws cloudwatch put-metric-alarm \
    --alarm-name "StudentEvaluator-HighCPU" \
    --alarm-description "High CPU utilization" \
    --metric-name CPUUtilization \
    --namespace AWS/ECS \
    --statistic Average \
    --period 300 \
    --threshold 80 \
    --comparison-operator GreaterThanThreshold \
    --evaluation-periods 2 \
    --alarm-actions arn:aws:sns:us-east-1:account:student-evaluator-alerts

# High error rate alarm
aws cloudwatch put-metric-alarm \
    --alarm-name "StudentEvaluator-HighErrorRate" \
    --alarm-description "High error rate" \
    --metric-name ErrorRate \
    --namespace StudentEvaluator/Application \
    --statistic Average \
    --period 300 \
    --threshold 5 \
    --comparison-operator GreaterThanThreshold \
    --evaluation-periods 1 \
    --alarm-actions arn:aws:sns:us-east-1:account:student-evaluator-alerts
```

**Prometheus Alerting Rules:**
```yaml
# student-evaluator-rules.yml
groups:
  - name: student-evaluator
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }} errors per second"
      
      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"
          description: "95th percentile response time is {{ $value }} seconds"
      
      - alert: DatabaseConnectionFailure
        expr: hikaricp_connections_active == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Database connection failure"
          description: "No active database connections"
```

## Backup and Disaster Recovery

### 1. Database Backup

**RDS Automated Backups:**
```bash
# Configure automated backups
aws rds modify-db-instance \
    --db-instance-identifier student-evaluator-db \
    --backup-retention-period 30 \
    --preferred-backup-window "03:00-04:00" \
    --preferred-maintenance-window "sun:04:00-sun:05:00"

# Create manual snapshot
aws rds create-db-snapshot \
    --db-instance-identifier student-evaluator-db \
    --db-snapshot-identifier student-evaluator-snapshot-$(date +%Y%m%d%H%M%S)
```

**DynamoDB Backup:**
```bash
# Enable point-in-time recovery
aws dynamodb update-continuous-backups \
    --table-name evaluation-logs-prod \
    --point-in-time-recovery-specification PointInTimeRecoveryEnabled=true

# Create on-demand backup
aws dynamodb create-backup \
    --table-name evaluation-logs-prod \
    --backup-name evaluation-logs-backup-$(date +%Y%m%d%H%M%S)
```

### 2. Application Data Backup

**S3 Cross-Region Replication:**
```json
{
  "Role": "arn:aws:iam::account:role/replication-role",
  "Rules": [
    {
      "ID": "ReplicateEverything",
      "Status": "Enabled",
      "Priority": 1,
      "Filter": {},
      "Destination": {
        "Bucket": "arn:aws:s3:::student-evaluator-files-backup",
        "StorageClass": "STANDARD_IA"
      }
    }
  ]
}
```

### 3. Disaster Recovery Plan

**Multi-Region Setup:**
```bash
# Create resources in secondary region
aws configure set region us-west-2

# Create RDS read replica
aws rds create-db-instance-read-replica \
    --db-instance-identifier student-evaluator-db-replica \
    --source-db-instance-identifier arn:aws:rds:us-east-1:account:db:student-evaluator-db \
    --db-instance-class db.t3.medium

# Create DynamoDB global table
aws dynamodb create-global-table \
    --global-table-name evaluation-logs-prod \
    --replication-group RegionName=us-east-1 RegionName=us-west-2
```

## CI/CD Pipeline

### 1. GitHub Actions Workflow

**`.github/workflows/deploy-production.yml`:**
```yaml
name: Deploy to Production

on:
  push:
    branches: [main]
    tags: ['v*']

env:
  AWS_REGION: us-east-1
  ECR_REPOSITORY: student-evaluator
  ECS_SERVICE: student-evaluator-service
  ECS_CLUSTER: student-evaluator-cluster

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'adopt'
      
      - name: Cache Maven dependencies
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
      
      - name: Run tests
        run: mvn clean test
      
      - name: Run integration tests
        run: mvn verify -P integration-tests
      
      - name: Generate test report
        uses: dorny/test-reporter@v1
        if: success() || failure()
        with:
          name: Maven Tests
          path: target/surefire-reports/*.xml
          reporter: java-junit

  security-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Run Snyk security scan
        uses: snyk/actions/maven@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
        with:
          args: --severity-threshold=high

  build-and-deploy:
    needs: [test, security-scan]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'adopt'
      
      - name: Build application
        run: mvn clean package -DskipTests
      
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v2
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ env.AWS_REGION }}
      
      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v1
      
      - name: Build, tag, and push image to Amazon ECR
        id: build-image
        env:
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          IMAGE_TAG: ${{ github.sha }}
        run: |
          docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
          echo "image=$ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG" >> $GITHUB_OUTPUT
      
      - name: Update ECS service
        env:
          IMAGE_URI: ${{ steps.build-image.outputs.image }}
        run: |
          aws ecs update-service \
            --cluster $ECS_CLUSTER \
            --service $ECS_SERVICE \
            --force-new-deployment
      
      - name: Wait for deployment
        run: |
          aws ecs wait services-stable \
            --cluster $ECS_CLUSTER \
            --services $ECS_SERVICE
      
      - name: Verify deployment
        run: |
          # Get service endpoint
          ENDPOINT=$(aws elbv2 describe-load-balancers \
            --names student-evaluator-alb \
            --query 'LoadBalancers[0].DNSName' \
            --output text)
          
          # Health check
          curl -f https://$ENDPOINT/actuator/health
```

### 2. Blue-Green Deployment

**Blue-Green Deployment Script:**
```bash
#!/bin/bash

set -e

# Configuration
CLUSTER_NAME="student-evaluator-cluster"
SERVICE_NAME="student-evaluator-service"
NEW_IMAGE="$1"

if [ -z "$NEW_IMAGE" ]; then
    echo "Usage: $0 <new-image-uri>"
    exit 1
fi

echo "Starting blue-green deployment..."

# Get current task definition
CURRENT_TASK_DEF=$(aws ecs describe-services \
    --cluster $CLUSTER_NAME \
    --services $SERVICE_NAME \
    --query 'services[0].taskDefinition' \
    --output text)

echo "Current task definition: $CURRENT_TASK_DEF"

# Create new task definition with new image
NEW_TASK_DEF=$(aws ecs describe-task-definition \
    --task-definition $CURRENT_TASK_DEF \
    --query 'taskDefinition' \
    --output json | \
    jq --arg IMAGE "$NEW_IMAGE" '.containerDefinitions[0].image = $IMAGE' | \
    jq 'del(.taskDefinitionArn, .revision, .status, .requiresAttributes, .placementConstraints, .compatibilities, .registeredAt, .registeredBy)')

# Register new task definition
NEW_TASK_DEF_ARN=$(echo $NEW_TASK_DEF | \
    aws ecs register-task-definition \
    --cli-input-json file:///dev/stdin \
    --query 'taskDefinition.taskDefinitionArn' \
    --output text)

echo "New task definition: $NEW_TASK_DEF_ARN"

# Update service with new task definition
aws ecs update-service \
    --cluster $CLUSTER_NAME \
    --service $SERVICE_NAME \
    --task-definition $NEW_TASK_DEF_ARN

echo "Waiting for deployment to complete..."

# Wait for deployment to stabilize
aws ecs wait services-stable \
    --cluster $CLUSTER_NAME \
    --services $SERVICE_NAME

echo "Deployment completed successfully!"

# Verify health
ENDPOINT=$(aws elbv2 describe-load-balancers \
    --names student-evaluator-alb \
    --query 'LoadBalancers[0].DNSName' \
    --output text)

if curl -f https://$ENDPOINT/actuator/health; then
    echo "Health check passed!"
else
    echo "Health check failed! Rolling back..."
    
    # Rollback to previous task definition
    aws ecs update-service \
        --cluster $CLUSTER_NAME \
        --service $SERVICE_NAME \
        --task-definition $CURRENT_TASK_DEF
    
    exit 1
fi
```

## Performance Optimization

### 1. JVM Tuning

**Production JVM Options:**
```bash
JAVA_OPTS="-Xms2g -Xmx4g \
           -XX:+UseG1GC \
           -XX:MaxGCPauseMillis=200 \
           -XX:+UseContainerSupport \
           -XX:MaxRAMPercentage=75.0 \
           -XX:+HeapDumpOnOutOfMemoryError \
           -XX:HeapDumpPath=/app/logs/heapdump.hprof \
           -XX:+UseStringDeduplication \
           -XX:+OptimizeStringConcat \
           -Djava.security.egd=file:/dev/./urandom"
```

### 2. Database Optimization

**MySQL Configuration:**
```ini
# my.cnf
[mysqld]
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
innodb_flush_method = O_DIRECT
max_connections = 200
query_cache_size = 128M
query_cache_type = 1
slow_query_log = 1
long_query_time = 2
```

**Connection Pool Configuration:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
      leak-detection-threshold: 60000
```

### 3. Caching Strategy

**Redis Configuration:**
```yaml
spring:
  redis:
    host: ${REDIS_HOST}
    port: 6379
    password: ${REDIS_PASSWORD}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
      shutdown-timeout: 100ms

cache:
  redis:
    time-to-live: 600000  # 10 minutes
    cache-null-values: false
```

## Troubleshooting

### 1. Common Issues

**High Memory Usage:**
```bash
# Check memory usage
kubectl top pods -n student-evaluator

# Get detailed memory info
kubectl describe pod <pod-name> -n student-evaluator

# Check JVM heap usage
curl http://pod-ip:8080/actuator/metrics/jvm.memory.used
```

**Database Connection Issues:**
```bash
# Check database connectivity
kubectl exec -it <pod-name> -n student-evaluator -- \
  mysql -h mysql-service -u evaluator -p -e "SELECT 1"

# Check connection pool metrics
curl http://pod-ip:8080/actuator/metrics/hikaricp.connections.active
```

**High Response Times:**
```bash
# Check response time metrics
curl http://pod-ip:8080/actuator/metrics/http.server.requests

# Check thread pool status
curl http://pod-ip:8080/actuator/metrics/executor.active
```

### 2. Log Analysis

**Centralized Logging with ELK:**
```yaml
# logstash.conf
input {
  beats {
    port => 5044
  }
}

filter {
  if [fields][service] == "student-evaluator" {
    grok {
      match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} \[%{DATA:thread}\] %{LOGLEVEL:level} \[%{DATA:trace_id},%{DATA:span_id}\] %{DATA:logger} - %{GREEDYDATA:message}" }
    }
    
    date {
      match => [ "timestamp", "yyyy-MM-dd HH:mm:ss" ]
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "student-evaluator-%{+YYYY.MM.dd}"
  }
}
```

### 3. Performance Monitoring

**Custom Metrics:**
```java
@Component
public class CustomMetrics {
    private final Counter evaluationCounter;
    private final Timer evaluationTimer;
    private final Gauge activeEvaluations;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.evaluationCounter = Counter.builder("evaluations.total")
            .description("Total number of evaluations")
            .register(meterRegistry);
            
        this.evaluationTimer = Timer.builder("evaluations.duration")
            .description("Evaluation processing time")
            .register(meterRegistry);
            
        this.activeEvaluations = Gauge.builder("evaluations.active")
            .description("Number of active evaluations")
            .register(meterRegistry, this, CustomMetrics::getActiveEvaluations);
    }
    
    private double getActiveEvaluations() {
        // Return current active evaluations count
        return evaluationService.getActiveEvaluationsCount();
    }
}
```

This production deployment guide provides comprehensive instructions for deploying the Student Evaluator System in production environments with proper security, monitoring, and operational procedures.