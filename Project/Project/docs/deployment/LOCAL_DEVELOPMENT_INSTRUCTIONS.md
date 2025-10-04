# Student Evaluator System - Local Development Guide

## Overview

This guide provides step-by-step instructions for setting up the Student Evaluator System for local development. It covers all prerequisites, installation steps, configuration, and common development workflows.

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 11 or higher**
   ```bash
   # Check Java version
   java -version
   javac -version
   
   # Install OpenJDK 11 (Ubuntu/Debian)
   sudo apt update
   sudo apt install openjdk-11-jdk
   
   # Install OpenJDK 11 (macOS with Homebrew)
   brew install openjdk@11
   
   # Install OpenJDK 11 (Windows)
   # Download from https://adoptopenjdk.net/
   ```

2. **Apache Maven 3.6 or higher**
   ```bash
   # Check Maven version
   mvn -version
   
   # Install Maven (Ubuntu/Debian)
   sudo apt install maven
   
   # Install Maven (macOS with Homebrew)
   brew install maven
   
   # Install Maven (Windows)
   # Download from https://maven.apache.org/download.cgi
   ```

3. **Git**
   ```bash
   # Check Git version
   git --version
   
   # Install Git (Ubuntu/Debian)
   sudo apt install git
   
   # Install Git (macOS with Homebrew)
   brew install git
   
   # Install Git (Windows)
   # Download from https://git-scm.com/download/win
   ```

4. **Docker and Docker Compose** (for database services)
   ```bash
   # Install Docker (Ubuntu/Debian)
   sudo apt update
   sudo apt install docker.io docker-compose
   sudo systemctl start docker
   sudo systemctl enable docker
   sudo usermod -aG docker $USER
   
   # Install Docker Desktop (macOS/Windows)
   # Download from https://www.docker.com/products/docker-desktop
   ```

### Optional Software

1. **IDE/Editor**
   - IntelliJ IDEA (recommended)
   - Visual Studio Code
   - Eclipse

2. **Database Clients**
   - MySQL Workbench
   - DBeaver
   - phpMyAdmin

3. **API Testing Tools**
   - Postman
   - Insomnia
   - curl

## Project Setup

### 1. Clone the Repository

```bash
# Clone the repository
git clone https://github.com/your-org/student-evaluator-system.git
cd student-evaluator-system

# Check project structure
ls -la
```

### 2. Environment Configuration

Create environment configuration files:

```bash
# Create local environment file
cp .env.example .env.local

# Edit the environment file
nano .env.local
```

**`.env.local` example:**
```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=student_evaluator_dev
DB_USERNAME=evaluator
DB_PASSWORD=dev_password

# DynamoDB Configuration
DYNAMODB_ENDPOINT=http://localhost:8000
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=dummy
AWS_SECRET_ACCESS_KEY=dummy

# Application Configuration
SERVER_PORT=8080
LOG_LEVEL=DEBUG
TEMP_DIR=/tmp/evaluations

# Development Settings
SPRING_PROFILES_ACTIVE=dev
DDL_AUTO=create-drop
SHOW_SQL=true
```

### 3. Database Setup

#### Option A: Using Docker Compose (Recommended)

```bash
# Start database services
docker-compose -f docker-compose.dev.yml up -d

# Check services are running
docker-compose -f docker-compose.dev.yml ps

# View logs
docker-compose -f docker-compose.dev.yml logs -f
```

**`docker-compose.dev.yml`:**
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: student-evaluator-mysql
    environment:
      MYSQL_DATABASE: student_evaluator_dev
      MYSQL_USER: evaluator
      MYSQL_PASSWORD: dev_password
      MYSQL_ROOT_PASSWORD: root_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_dev_data:/var/lib/mysql
      - ./scripts/init-db.sql:/docker-entrypoint-initdb.d/init-db.sql
    command: --default-authentication-plugin=mysql_native_password

  dynamodb:
    image: amazon/dynamodb-local:latest
    container_name: student-evaluator-dynamodb
    ports:
      - "8000:8000"
    command: ["-jar", "DynamoDBLocal.jar", "-sharedDb", "-inMemory"]

  adminer:
    image: adminer
    container_name: student-evaluator-adminer
    ports:
      - "8081:8080"
    depends_on:
      - mysql

volumes:
  mysql_dev_data:
```

#### Option B: Local Installation

**MySQL Setup:**
```bash
# Install MySQL (Ubuntu/Debian)
sudo apt update
sudo apt install mysql-server

# Secure installation
sudo mysql_secure_installation

# Create database and user
sudo mysql -u root -p
```

```sql
-- Create database
CREATE DATABASE student_evaluator_dev;

-- Create user
CREATE USER 'evaluator'@'localhost' IDENTIFIED BY 'dev_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON student_evaluator_dev.* TO 'evaluator'@'localhost';
FLUSH PRIVILEGES;

-- Exit MySQL
EXIT;
```

**DynamoDB Local Setup:**
```bash
# Download DynamoDB Local
wget https://s3.us-west-2.amazonaws.com/dynamodb-local/dynamodb_local_latest.tar.gz
tar -xzf dynamodb_local_latest.tar.gz

# Start DynamoDB Local
java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb -port 8000
```

### 4. Build the Project

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package the application
mvn package

# Skip tests during packaging (if needed)
mvn package -DskipTests
```

### 5. Initialize Database Schema

```bash
# Run database migrations (if using Flyway)
mvn flyway:migrate

# Or let Spring Boot create schema automatically
# (set spring.jpa.hibernate.ddl-auto=create-drop in application-dev.yml)
```

### 6. Create DynamoDB Tables

```bash
# Create DynamoDB tables using AWS CLI
aws dynamodb create-table \
    --table-name evaluation_logs \
    --attribute-definitions \
        AttributeName=evaluation_id,AttributeType=S \
        AttributeName=timestamp,AttributeType=S \
    --key-schema \
        AttributeName=evaluation_id,KeyType=HASH \
        AttributeName=timestamp,KeyType=RANGE \
    --provisioned-throughput \
        ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --endpoint-url http://localhost:8000

# Or use the provided script
./scripts/setup-dynamodb-local.sh
```

## Running the Application

### 1. Start the Main Application

```bash
# Run with Maven
mvn spring-boot:run

# Or run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or run the JAR file
java -jar target/student-evaluator-1.0.0.jar

# Run with custom configuration
java -jar target/student-evaluator-1.0.0.jar --spring.config.location=classpath:/application-dev.yml
```

### 2. Verify Application Startup

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check application info
curl http://localhost:8080/actuator/info

# Check available endpoints
curl http://localhost:8080/actuator/mappings
```

Expected health response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 250685575168,
        "free": 71384276992,
        "threshold": 10485760,
        "exists": true
      }
    }
  }
}
```

### 3. Test API Endpoints

```bash
# Test assignment upload
curl -X POST http://localhost:8080/api/v1/assignments \
  -F "file=@sample-files/Calculator.java" \
  -F "title=Test Assignment" \
  -F "description=Test assignment for development"

# Test evaluation trigger
curl -X POST http://localhost:8080/api/v1/evaluations \
  -F "file=@sample-files/StudentCalculator.java" \
  -F "studentId=dev-student-1" \
  -F "assignmentId=test-assignment-1"
```

## Development Workflow

### 1. IDE Setup

#### IntelliJ IDEA Configuration

1. **Import Project:**
   - File → Open → Select project directory
   - Choose "Import project from external model" → Maven
   - Use default settings

2. **Configure Run Configuration:**
   - Run → Edit Configurations
   - Add new "Spring Boot" configuration
   - Main class: `com.studentevaluator.StudentEvaluatorApplication`
   - VM options: `-Dspring.profiles.active=dev`
   - Environment variables: Load from `.env.local`

3. **Enable Annotation Processing:**
   - File → Settings → Build → Compiler → Annotation Processors
   - Check "Enable annotation processing"

4. **Code Style:**
   - File → Settings → Editor → Code Style → Java
   - Import code style from `docs/dev-tools/intellij-code-style.xml`

#### Visual Studio Code Configuration

1. **Install Extensions:**
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - REST Client

2. **Configure Workspace:**
   ```json
   // .vscode/settings.json
   {
     "java.configuration.updateBuildConfiguration": "automatic",
     "java.compile.nullAnalysis.mode": "automatic",
     "spring-boot.ls.problem.application-properties.unknown-property": "ignore"
   }
   ```

3. **Launch Configuration:**
   ```json
   // .vscode/launch.json
   {
     "version": "0.2.0",
     "configurations": [
       {
         "type": "java",
         "name": "Spring Boot-StudentEvaluatorApplication",
         "request": "launch",
         "cwd": "${workspaceFolder}",
         "mainClass": "com.studentevaluator.StudentEvaluatorApplication",
         "projectName": "student-evaluator",
         "args": "--spring.profiles.active=dev",
         "envFile": "${workspaceFolder}/.env.local"
       }
     ]
   }
   ```

### 2. Hot Reload Setup

Add Spring Boot DevTools for automatic restart:

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

Configure automatic build:
```properties
# application-dev.yml
spring:
  devtools:
    restart:
      enabled: true
    livereload:
      enabled: true
```

### 3. Testing Workflow

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=EvaluationServiceTest

# Run tests with specific profile
mvn test -Dspring.profiles.active=test

# Run integration tests only
mvn test -Dtest=**/*IntegrationTest

# Run tests with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### 4. Database Management

#### View Database Content

```bash
# Connect to MySQL
mysql -h localhost -u evaluator -p student_evaluator_dev

# Common queries
SELECT * FROM students;
SELECT * FROM assignments;
SELECT * FROM evaluations ORDER BY evaluated_at DESC LIMIT 10;

# Check table structure
DESCRIBE evaluations;
SHOW CREATE TABLE evaluations;
```

#### DynamoDB Management

```bash
# List tables
aws dynamodb list-tables --endpoint-url http://localhost:8000

# Scan evaluation logs
aws dynamodb scan \
    --table-name evaluation_logs \
    --endpoint-url http://localhost:8000

# Query specific evaluation
aws dynamodb query \
    --table-name evaluation_logs \
    --key-condition-expression "evaluation_id = :eval_id" \
    --expression-attribute-values '{":eval_id":{"S":"eval-123-456"}}' \
    --endpoint-url http://localhost:8000
```

### 5. API Development and Testing

#### Using Postman

1. **Import Collection:**
   - Import `docs/api/postman-collection.json`
   - Set environment variables for local development

2. **Environment Variables:**
   ```json
   {
     "base_url": "http://localhost:8080/api/v1",
     "student_id": "dev-student-1",
     "assignment_id": "test-assignment-1"
   }
   ```

#### Using REST Client (VS Code)

Create `api-tests.http`:
```http
### Upload Assignment
POST {{base_url}}/assignments
Content-Type: multipart/form-data; boundary=boundary

--boundary
Content-Disposition: form-data; name="file"; filename="Calculator.java"
Content-Type: text/plain

< ./sample-files/Calculator.java
--boundary
Content-Disposition: form-data; name="title"

Test Calculator Assignment
--boundary
Content-Disposition: form-data; name="description"

Basic calculator for testing
--boundary--

### Trigger Evaluation
POST {{base_url}}/evaluations
Content-Type: multipart/form-data; boundary=boundary

--boundary
Content-Disposition: form-data; name="file"; filename="StudentCalculator.java"
Content-Type: text/plain

< ./sample-files/StudentCalculator.java
--boundary
Content-Disposition: form-data; name="studentId"

dev-student-1
--boundary
Content-Disposition: form-data; name="assignmentId"

{{assignment_id}}
--boundary--

### Get Evaluation Results
GET {{base_url}}/evaluations/{{evaluation_id}}/results
```

## Debugging

### 1. Application Debugging

#### Enable Debug Logging

```yaml
# application-dev.yml
logging:
  level:
    com.studentevaluator: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### Remote Debugging

```bash
# Start application with debug port
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
     -jar target/student-evaluator-1.0.0.jar

# Or with Maven
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

Configure IDE to connect to port 5005 for remote debugging.

### 2. Database Debugging

#### Enable SQL Logging

```yaml
# application-dev.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### Database Connection Issues

```bash
# Test MySQL connection
mysql -h localhost -u evaluator -p -e "SELECT 1"

# Check MySQL service status
sudo systemctl status mysql

# Check Docker containers
docker ps
docker logs student-evaluator-mysql
```

### 3. Common Issues and Solutions

#### Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080
netstat -tulpn | grep :8080

# Kill process
kill -9 <PID>

# Or use different port
java -jar target/student-evaluator-1.0.0.jar --server.port=8081
```

#### Out of Memory Errors

```bash
# Increase heap size
java -Xmx2g -jar target/student-evaluator-1.0.0.jar

# Monitor memory usage
jstat -gc <PID>
jmap -histo <PID>
```

#### File Permission Issues

```bash
# Fix temp directory permissions
sudo mkdir -p /tmp/evaluations
sudo chown $USER:$USER /tmp/evaluations
sudo chmod 755 /tmp/evaluations

# Or use user temp directory
export TEMP_DIR=$HOME/tmp/evaluations
mkdir -p $TEMP_DIR
```

## Sample Data Setup

### 1. Create Sample Files

```bash
# Create sample directory
mkdir -p sample-files

# Create sample assignment
cat > sample-files/Calculator.java << 'EOF'
public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
    
    public int subtract(int a, int b) {
        return a - b;
    }
    
    public int multiply(int a, int b) {
        return a * b;
    }
    
    public int divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return a / b;
    }
}
EOF

# Create sample test
cat > sample-files/CalculatorTest.java << 'EOF'
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class CalculatorTest {
    private Calculator calculator;
    
    @BeforeEach
    void setUp() {
        calculator = new Calculator();
    }
    
    @Test
    void testAdd() {
        assertEquals(5, calculator.add(2, 3));
        assertEquals(0, calculator.add(-1, 1));
    }
    
    @Test
    void testSubtract() {
        assertEquals(1, calculator.subtract(3, 2));
        assertEquals(-2, calculator.subtract(-1, 1));
    }
    
    @Test
    void testMultiply() {
        assertEquals(6, calculator.multiply(2, 3));
        assertEquals(0, calculator.multiply(0, 5));
    }
    
    @Test
    void testDivide() {
        assertEquals(2, calculator.divide(6, 3));
        assertThrows(ArithmeticException.class, () -> calculator.divide(1, 0));
    }
}
EOF

# Create student submission (with intentional bug)
cat > sample-files/StudentCalculator.java << 'EOF'
public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
    
    public int subtract(int a, int b) {
        return a - b;
    }
    
    public int multiply(int a, int b) {
        return a * b;
    }
    
    public int divide(int a, int b) {
        // Bug: missing zero check
        return a / b;
    }
}
EOF
```

### 2. Load Sample Data

```bash
# Run sample data script
./scripts/load-sample-data.sh

# Or manually load data
curl -X POST http://localhost:8080/api/v1/assignments \
  -F "file=@sample-files/Calculator.java" \
  -F "title=Calculator Assignment" \
  -F "description=Basic calculator implementation"
```

## Performance Monitoring

### 1. Application Metrics

```bash
# View metrics endpoint
curl http://localhost:8080/actuator/metrics

# View specific metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/http.server.requests
```

### 2. Database Performance

```sql
-- Enable MySQL slow query log
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;

-- View slow queries
SELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 10;

-- Check connection status
SHOW STATUS LIKE 'Connections';
SHOW STATUS LIKE 'Threads_connected';
```

### 3. JVM Monitoring

```bash
# JVM process info
jps -l

# Memory usage
jstat -gc <PID> 1s

# Thread dump
jstack <PID> > thread-dump.txt

# Heap dump
jmap -dump:format=b,file=heap-dump.hprof <PID>
```

## Backup and Recovery

### 1. Database Backup

```bash
# MySQL backup
mysqldump -h localhost -u evaluator -p student_evaluator_dev > backup.sql

# Restore MySQL backup
mysql -h localhost -u evaluator -p student_evaluator_dev < backup.sql

# DynamoDB backup (export to JSON)
aws dynamodb scan \
    --table-name evaluation_logs \
    --endpoint-url http://localhost:8000 \
    --output json > dynamodb-backup.json
```

### 2. File System Backup

```bash
# Backup uploaded files
tar -czf files-backup.tar.gz /tmp/evaluations/

# Backup configuration
cp -r .env.local config/ backup/
```

## Cleanup

### 1. Stop Services

```bash
# Stop application
# Ctrl+C or kill process

# Stop Docker services
docker-compose -f docker-compose.dev.yml down

# Remove volumes (optional)
docker-compose -f docker-compose.dev.yml down -v
```

### 2. Clean Build Artifacts

```bash
# Maven clean
mvn clean

# Remove temporary files
rm -rf /tmp/evaluations/*

# Remove logs
rm -f logs/*.log
```

This local development guide provides everything needed to set up and run the Student Evaluator System in a development environment. Follow the steps in order, and refer to the troubleshooting section if you encounter any issues.