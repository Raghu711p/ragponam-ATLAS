# Database Setup Guide

This guide covers setting up both SQL and DynamoDB storage for the Student Evaluator System using SQL Workbench/J and DynamoDB Workbench.

## Prerequisites

- Java 8 or higher
- MySQL Server (for SQL storage)
- AWS CLI configured (for DynamoDB)
- DynamoDB Local JAR file (for local development)

## Part 1: SQL Database Setup with SQL Workbench/J

### 1.1 Install SQL Workbench/J

1. Download SQL Workbench/J from: https://www.sql-workbench.eu/downloads.html
2. Extract the archive to your preferred directory
3. Download MySQL JDBC driver (mysql-connector-java) from: https://dev.mysql.com/downloads/connector/j/

### 1.2 Configure MySQL Connection

1. Start SQL Workbench/J
2. Click "Manage Drivers" in the connection dialog
3. Add MySQL driver:
   - Name: `MySQL`
   - Library: Browse to your `mysql-connector-java-x.x.x.jar`
   - Classname: `com.mysql.cj.jdbc.Driver`
   - Sample URL: `jdbc:mysql://localhost:3306/database`

### 1.3 Create Database Connection

1. In SQL Workbench/J, create new connection:
   - Driver: MySQL
   - URL: `jdbc:mysql://localhost:3306/student_evaluator`
   - Username: `evaluator`
   - Password: `password`
   - Test connection

### 1.4 Initialize Database Schema

Execute the following SQL to create the database structure:

```sql
-- Create database
CREATE DATABASE IF NOT EXISTS student_evaluator;
USE student_evaluator;

-- Students table
CREATE TABLE students (
    student_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Assignments table
CREATE TABLE assignments (
    assignment_id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    test_file_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Evaluations table
CREATE TABLE evaluations (
    evaluation_id VARCHAR(50) PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    assignment_id VARCHAR(50) NOT NULL,
    score DECIMAL(5,2) DEFAULT 0.00,
    max_score DECIMAL(5,2) DEFAULT 100.00,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    submission_file_path VARCHAR(500),
    compilation_output TEXT,
    test_results TEXT,
    evaluated_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id) ON DELETE CASCADE,
    INDEX idx_student_assignment (student_id, assignment_id),
    INDEX idx_status (status),
    INDEX idx_evaluated_at (evaluated_at)
);

-- Create indexes for better performance
CREATE INDEX idx_students_email ON students(email);
CREATE INDEX idx_assignments_title ON assignments(title);
CREATE INDEX idx_evaluations_score ON evaluations(score);
```

### 1.5 Insert Sample Data

```sql
-- Insert sample students
INSERT INTO students (student_id, name, email) VALUES
('STU001', 'John Doe', 'john.doe@example.com'),
('STU002', 'Jane Smith', 'jane.smith@example.com'),
('STU003', 'Bob Johnson', 'bob.johnson@example.com');

-- Insert sample assignments
INSERT INTO assignments (assignment_id, title, description, test_file_path) VALUES
('ASG001', 'Calculator Implementation', 'Implement a basic calculator with add, subtract, multiply, divide operations', '/tests/CalculatorTest.java'),
('ASG002', 'String Utilities', 'Create utility methods for string manipulation', '/tests/StringUtilsTest.java'),
('ASG003', 'Data Structures', 'Implement basic data structures: Stack, Queue, LinkedList', '/tests/DataStructuresTest.java');

-- Insert sample evaluations
INSERT INTO evaluations (evaluation_id, student_id, assignment_id, score, max_score, status, evaluated_at) VALUES
('EVAL001', 'STU001', 'ASG001', 85.50, 100.00, 'COMPLETED', NOW()),
('EVAL002', 'STU001', 'ASG002', 92.00, 100.00, 'COMPLETED', NOW()),
('EVAL003', 'STU002', 'ASG001', 78.25, 100.00, 'COMPLETED', NOW()),
('EVAL004', 'STU003', 'ASG001', 95.00, 100.00, 'COMPLETED', NOW());
```

## Part 2: DynamoDB Setup with DynamoDB Workbench

### 2.1 Install DynamoDB Workbench and Local DynamoDB

#### Option A: AWS DynamoDB (Production)
1. Install AWS CLI: https://aws.amazon.com/cli/
2. Configure credentials: `aws configure`
3. Download NoSQL Workbench for DynamoDB: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/workbench.html

#### Option B: Local DynamoDB (Development)
1. Download DynamoDB Local JAR:
   - Visit: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.DownloadingAndRunning.html
   - Download `dynamodb_local_latest.tar.gz`
   - Extract to a directory (e.g., `C:\dynamodb-local` on Windows)

2. Start DynamoDB Local:
   ```cmd
   cd C:\dynamodb-local
   java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb -port 8000
   ```

3. Verify it's running:
   ```cmd
   curl http://localhost:8000
   ```
   You should see a response indicating DynamoDB Local is running.

### 2.2 Configure DynamoDB Connection

#### For AWS DynamoDB:
1. Open NoSQL Workbench
2. Click "Add connection"
3. Select "DynamoDB"
4. Choose your AWS region
5. Use your AWS credentials

#### For Local DynamoDB:
1. Open NoSQL Workbench
2. Click "Add connection"
3. Select "DynamoDB Local"
4. Endpoint: `http://localhost:8000`

### 2.3 Create DynamoDB Tables

#### Create evaluation_logs table:

```json
{
  "TableName": "evaluation_logs",
  "KeySchema": [
    {
      "AttributeName": "evaluation_id",
      "KeyType": "HASH"
    },
    {
      "AttributeName": "timestamp",
      "KeyType": "RANGE"
    }
  ],
  "AttributeDefinitions": [
    {
      "AttributeName": "evaluation_id",
      "AttributeType": "S"
    },
    {
      "AttributeName": "timestamp",
      "AttributeType": "S"
    },
    {
      "AttributeName": "student_id",
      "AttributeType": "S"
    }
  ],
  "GlobalSecondaryIndexes": [
    {
      "IndexName": "StudentIndex",
      "KeySchema": [
        {
          "AttributeName": "student_id",
          "KeyType": "HASH"
        },
        {
          "AttributeName": "timestamp",
          "KeyType": "RANGE"
        }
      ],
      "Projection": {
        "ProjectionType": "ALL"
      },
      "BillingMode": "PAY_PER_REQUEST"
    }
  ],
  "BillingMode": "PAY_PER_REQUEST"
}
```

### 2.4 Using AWS CLI to Create Tables

#### For Local DynamoDB:
```cmd
# Create evaluation_logs table (Windows)
aws dynamodb create-table ^
    --endpoint-url http://localhost:8000 ^
    --table-name evaluation_logs ^
    --attribute-definitions ^
        AttributeName=evaluation_id,AttributeType=S ^
        AttributeName=timestamp,AttributeType=S ^
        AttributeName=student_id,AttributeType=S ^
    --key-schema ^
        AttributeName=evaluation_id,KeyType=HASH ^
        AttributeName=timestamp,KeyType=RANGE ^
    --global-secondary-indexes ^
        IndexName=StudentIndex,KeySchema=[{AttributeName=student_id,KeyType=HASH},{AttributeName=timestamp,KeyType=RANGE}],Projection={ProjectionType=ALL} ^
    --billing-mode PAY_PER_REQUEST
```

#### For AWS DynamoDB (Production):
```cmd
# Create evaluation_logs table (remove --endpoint-url for AWS)
aws dynamodb create-table ^
    --table-name evaluation_logs ^
    --attribute-definitions ^
        AttributeName=evaluation_id,AttributeType=S ^
        AttributeName=timestamp,AttributeType=S ^
        AttributeName=student_id,AttributeType=S ^
    --key-schema ^
        AttributeName=evaluation_id,KeyType=HASH ^
        AttributeName=timestamp,KeyType=RANGE ^
    --global-secondary-indexes ^
        IndexName=StudentIndex,KeySchema=[{AttributeName=student_id,KeyType=HASH},{AttributeName=timestamp,KeyType=RANGE}],Projection={ProjectionType=ALL} ^
    --billing-mode PAY_PER_REQUEST
```

### 2.5 Insert Sample Data into DynamoDB

#### For Local DynamoDB:
```cmd
# Insert sample evaluation log
aws dynamodb put-item ^
    --endpoint-url http://localhost:8000 ^
    --table-name evaluation_logs ^
    --item "{\"evaluation_id\": {\"S\": \"EVAL001\"}, \"timestamp\": {\"S\": \"2024-01-15T10:30:00Z\"}, \"student_id\": {\"S\": \"STU001\"}, \"assignment_id\": {\"S\": \"ASG001\"}, \"action\": {\"S\": \"EVALUATION_STARTED\"}, \"details\": {\"S\": \"Student submitted Calculator.java for evaluation\"}, \"metadata\": {\"M\": {\"file_size\": {\"N\": \"1024\"}, \"submission_time\": {\"S\": \"2024-01-15T10:29:45Z\"}}}}"
```

#### For AWS DynamoDB:
```cmd
# Insert sample evaluation log (remove --endpoint-url for AWS)
aws dynamodb put-item ^
    --table-name evaluation_logs ^
    --item "{\"evaluation_id\": {\"S\": \"EVAL001\"}, \"timestamp\": {\"S\": \"2024-01-15T10:30:00Z\"}, \"student_id\": {\"S\": \"STU001\"}, \"assignment_id\": {\"S\": \"ASG001\"}, \"action\": {\"S\": \"EVALUATION_STARTED\"}, \"details\": {\"S\": \"Student submitted Calculator.java for evaluation\"}, \"metadata\": {\"M\": {\"file_size\": {\"N\": \"1024\"}, \"submission_time\": {\"S\": \"2024-01-15T10:29:45Z\"}}}}"
```

## Part 3: Application Configuration

### 3.1 Update application.yml

Ensure your `application.yml` has the correct database configurations:

```yaml
# For development with local databases
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:mysql://localhost:3306/student_evaluator
    username: evaluator
    password: password

aws:
  region: us-east-1
  dynamodb:
    endpoint: http://localhost:8000  # Remove for AWS DynamoDB
    table-name: evaluation_logs
```

### 3.2 Environment Variables

Set these environment variables for production:

```bash
# MySQL Configuration
export DB_HOST=your-mysql-host
export DB_PORT=3306
export DB_NAME=student_evaluator
export DB_USERNAME=your-username
export DB_PASSWORD=your-password

# AWS Configuration
export AWS_REGION=us-east-1
export DYNAMODB_TABLE=evaluation_logs
# Remove DYNAMODB_ENDPOINT for AWS DynamoDB
```

## Part 4: Testing Database Connections

### 4.1 Test SQL Connection

Run this query in SQL Workbench/J:

```sql
SELECT 
    s.name as student_name,
    a.title as assignment_title,
    e.score,
    e.status,
    e.evaluated_at
FROM evaluations e
JOIN students s ON e.student_id = s.student_id
JOIN assignments a ON e.assignment_id = a.assignment_id
ORDER BY e.evaluated_at DESC;
```

### 4.2 Test DynamoDB Connection

#### For Local DynamoDB:
```cmd
# List tables
aws dynamodb list-tables --endpoint-url http://localhost:8000

# Query evaluation logs
aws dynamodb query ^
    --endpoint-url http://localhost:8000 ^
    --table-name evaluation_logs ^
    --key-condition-expression "evaluation_id = :eval_id" ^
    --expression-attribute-values "{\":eval_id\": {\"S\": \"EVAL001\"}}"
```

#### For AWS DynamoDB:
```cmd
# List tables
aws dynamodb list-tables

# Query evaluation logs
aws dynamodb query ^
    --table-name evaluation_logs ^
    --key-condition-expression "evaluation_id = :eval_id" ^
    --expression-attribute-values "{\":eval_id\": {\"S\": \"EVAL001\"}}"
```

## Part 5: Quick Setup Scripts (Windows)

For faster setup, use the provided batch scripts:

### 5.1 MySQL Setup Script

Run the MySQL setup script:
```cmd
scripts\setup-mysql-database.bat
```

This script will:
- Create the `student_evaluator` database
- Create the `evaluator` user with password `password`
- Create all required tables with proper indexes
- Insert sample data for testing

### 5.2 DynamoDB Local Setup Scripts

#### Start DynamoDB Local:
```cmd
scripts\start-dynamodb-local.bat
```

#### Setup DynamoDB Tables:
```cmd
scripts\setup-dynamodb-tables.bat
```

This script will:
- Create the `evaluation_logs` table with proper schema
- Create the StudentIndex global secondary index
- Insert sample evaluation log data

### 5.3 Complete Local Setup

For a complete local development setup:

1. **Install Prerequisites:**
   - MySQL Server: https://dev.mysql.com/downloads/mysql/
   - AWS CLI: https://aws.amazon.com/cli/
   - DynamoDB Local: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.DownloadingAndRunning.html

2. **Extract DynamoDB Local to:** `C:\dynamodb-local`

3. **Run Setup Scripts:**
   ```cmd
   # Setup MySQL database
   scripts\setup-mysql-database.bat
   
   # Start DynamoDB Local (in separate command window)
   scripts\start-dynamodb-local.bat
   
   # Setup DynamoDB tables (in another command window)
   scripts\setup-dynamodb-tables.bat
   ```

4. **Start the Application:**
   ```cmd
   mvnw spring-boot:run
   ```

## Part 6: Backup and Maintenance

### 6.1 MySQL Backup

```bash
# Create backup
mysqldump -u evaluator -p student_evaluator > backup_$(date +%Y%m%d).sql

# Restore backup
mysql -u evaluator -p student_evaluator < backup_20240115.sql
```

### 6.2 DynamoDB Backup

```bash
# Create on-demand backup
aws dynamodb create-backup \
    --table-name evaluation_logs \
    --backup-name evaluation_logs_backup_$(date +%Y%m%d)

# Enable point-in-time recovery
aws dynamodb update-continuous-backups \
    --table-name evaluation_logs \
    --point-in-time-recovery-specification PointInTimeRecoveryEnabled=true
```

## Troubleshooting

### Common SQL Issues:
- **Connection refused**: Check if MySQL server is running
- **Access denied**: Verify username/password and user permissions
- **Table doesn't exist**: Run the schema creation scripts

### Common DynamoDB Issues:
- **Endpoint not found**: Check if DynamoDB Local is running on port 8000 using `start-dynamodb-local.bat`
- **Java not found**: Ensure Java is installed and in your PATH
- **DynamoDB Local JAR not found**: Download and extract DynamoDB Local to `C:\dynamodb-local`
- **Credentials error**: For local DynamoDB, ensure AWS CLI is installed (credentials not required for local)
- **Table not found**: Run `setup-dynamodb-tables.bat` to create tables
- **Port already in use**: Check if another process is using port 8000

### Application Issues:
- **DataSource error**: Check database URL and credentials in application.yml
- **DynamoDB client error**: Verify AWS region and endpoint configuration
- **Connection pool exhausted**: Adjust HikariCP settings in application.yml

## Next Steps

1. Run the application with `./mvnw spring-boot:run`
2. Test the REST API endpoints
3. Monitor database performance using workbench tools
4. Set up automated backups for production