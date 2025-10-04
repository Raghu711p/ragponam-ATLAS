# Student Evaluator System

A comprehensive Java assignment evaluation system that automates the compilation and testing of student submissions using JUnit tests.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── studentevaluator/
│   │           ├── StudentEvaluatorApplication.java  # Main Spring Boot application
│   │           ├── controller/                       # REST API controllers
│   │           ├── service/                         # Business logic services
│   │           ├── repository/                      # Data access layer
│   │           ├── model/                          # Entity and data models
│   │           ├── engine/                         # Compilation and testing engine
│   │           ├── cli/                            # Command-line interface
│   │           └── config/                         # Configuration classes
│   └── resources/
│       └── application.yml                         # Application configuration
└── test/
    ├── java/
    │   └── com/
    │       └── studentevaluator/                   # Test classes
    └── resources/
        ├── application-test.yml                    # Test configuration
        └── features/                               # BDD feature files
```

## Technologies Used

- **Spring Boot 3.2.0** - Main application framework
- **Spring Web** - REST API endpoints
- **Spring Data JPA** - Database abstraction layer
- **MySQL** - Primary database for structured data
- **AWS DynamoDB** - NoSQL database for unstructured logs
- **JUnit 5** - Testing framework
- **Picocli** - Command-line interface framework
- **Testcontainers** - Integration testing with real databases
- **Cucumber** - BDD testing framework

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- AWS CLI configured (for DynamoDB)

### Building the Project

```bash
mvn clean compile
```

### Running Tests

```bash
mvn test
```

### Running the Application

```bash
mvn spring-boot:run
```

## Configuration

The application can be configured through environment variables or by modifying `application.yml`:

- `DB_HOST` - MySQL database host (default: localhost)
- `DB_PORT` - MySQL database port (default: 3306)
- `DB_NAME` - Database name (default: student_evaluator)
- `DB_USERNAME` - Database username (default: evaluator)
- `DB_PASSWORD` - Database password (default: password)
- `AWS_REGION` - AWS region for DynamoDB (default: us-east-1)
- `DYNAMODB_ENDPOINT` - DynamoDB endpoint (leave empty for AWS)

## Development Profiles

- `dev` - Development profile with debug logging
- `test` - Test profile with H2 in-memory database
- `prod` - Production profile with optimized settings