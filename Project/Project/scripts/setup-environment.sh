#!/bin/bash

# Student Evaluator System - Environment Setup Script
# This script sets up the local development environment

set -e  # Exit on any error

echo "=========================================="
echo "Student Evaluator System - Environment Setup"
echo "=========================================="

# Configuration
JAVA_MIN_VERSION=17
MAVEN_MIN_VERSION="3.6.0"

# Function to compare versions
version_compare() {
    if [[ $1 == $2 ]]; then
        return 0
    fi
    local IFS=.
    local i ver1=($1) ver2=($2)
    for ((i=${#ver1[@]}; i<${#ver2[@]}; i++)); do
        ver1[i]=0
    done
    for ((i=0; i<${#ver1[@]}; i++)); do
        if [[ -z ${ver2[i]} ]]; then
            ver2[i]=0
        fi
        if ((10#${ver1[i]} > 10#${ver2[i]})); then
            return 1
        fi
        if ((10#${ver1[i]} < 10#${ver2[i]})); then
            return 2
        fi
    done
    return 0
}

# Check Java installation
echo "Checking Java installation..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
    if [[ "$JAVA_VERSION" =~ ^1\. ]]; then
        JAVA_VERSION=$(echo $JAVA_VERSION | cut -d'.' -f2)
    fi
    
    echo "Found Java version: $JAVA_VERSION"
    
    if [ "$JAVA_VERSION" -ge "$JAVA_MIN_VERSION" ]; then
        echo "✅ Java version is compatible"
    else
        echo "❌ Java version $JAVA_VERSION is too old. Minimum required: $JAVA_MIN_VERSION"
        echo "Please install Java $JAVA_MIN_VERSION or later"
        exit 1
    fi
else
    echo "❌ Java is not installed or not in PATH"
    echo "Please install Java $JAVA_MIN_VERSION or later"
    exit 1
fi

# Check Maven installation
echo ""
echo "Checking Maven installation..."
if command -v mvn &> /dev/null; then
    MAVEN_VERSION=$(mvn -version | head -n 1 | sed 's/Apache Maven \([0-9.]*\).*/\1/')
    echo "Found Maven version: $MAVEN_VERSION"
    
    version_compare $MAVEN_VERSION $MAVEN_MIN_VERSION
    case $? in
        0|1)
            echo "✅ Maven version is compatible"
            ;;
        2)
            echo "❌ Maven version $MAVEN_VERSION is too old. Minimum required: $MAVEN_MIN_VERSION"
            echo "Please install Maven $MAVEN_MIN_VERSION or later"
            exit 1
            ;;
    esac
else
    echo "❌ Maven is not installed or not in PATH"
    echo "Please install Maven $MAVEN_MIN_VERSION or later"
    exit 1
fi

# Check Git installation (optional but recommended)
echo ""
echo "Checking Git installation..."
if command -v git &> /dev/null; then
    GIT_VERSION=$(git --version | sed 's/git version \([0-9.]*\).*/\1/')
    echo "Found Git version: $GIT_VERSION"
    echo "✅ Git is available"
else
    echo "⚠️  Git is not installed (optional but recommended)"
fi

# Create necessary directories
echo ""
echo "Creating project directories..."
mkdir -p logs
mkdir -p data/assignments
mkdir -p data/submissions
mkdir -p data/test-files
mkdir -p target/test-reports

echo "✅ Project directories created"

# Set executable permissions for scripts
echo ""
echo "Setting executable permissions for scripts..."
chmod +x scripts/*.sh
echo "✅ Script permissions set"

# Validate Maven project
echo ""
echo "Validating Maven project..."
if [ -f "pom.xml" ]; then
    mvn validate
    if [ $? -eq 0 ]; then
        echo "✅ Maven project validation successful"
    else
        echo "❌ Maven project validation failed"
        exit 1
    fi
else
    echo "❌ pom.xml not found. Make sure you're in the project root directory"
    exit 1
fi

# Download dependencies
echo ""
echo "Downloading Maven dependencies..."
mvn dependency:resolve
if [ $? -eq 0 ]; then
    echo "✅ Dependencies downloaded successfully"
else
    echo "❌ Failed to download dependencies"
    exit 1
fi

# Create local configuration template
echo ""
echo "Creating local configuration template..."
cat > application-local.yml.template << EOF
# Local Development Configuration Template
# Copy this file to src/main/resources/application-local.yml and customize as needed

server:
  port: 8080

spring:
  profiles:
    active: local
    
  datasource:
    url: jdbc:h2:file:./data/studentevaluator;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
    
  h2:
    console:
      enabled: true
      path: /h2-console
      
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    
logging:
  level:
    com.studentevaluator: DEBUG
    org.springframework: INFO
  file:
    name: logs/application.log

# Application specific settings
app:
  storage:
    base-path: ./data
    assignments-path: ./data/assignments
    submissions-path: ./data/submissions
    test-files-path: ./data/test-files
  
  evaluation:
    timeout-seconds: 30
    max-concurrent-evaluations: 5
    
  security:
    max-file-size: 1048576  # 1MB
    allowed-extensions: [".java"]
EOF

echo "✅ Configuration template created: application-local.yml.template"

# Create README for local development
echo ""
echo "Creating local development README..."
cat > LOCAL_DEVELOPMENT.md << EOF
# Local Development Setup

This document describes how to set up and run the Student Evaluator System locally.

## Prerequisites

- Java $JAVA_MIN_VERSION or later
- Maven $MAVEN_MIN_VERSION or later
- Git (recommended)

## Quick Start

1. **Setup Environment**
   \`\`\`bash
   ./scripts/setup-environment.sh
   \`\`\`

2. **Build Application**
   \`\`\`bash
   ./scripts/build.sh
   \`\`\`

3. **Run Tests**
   \`\`\`bash
   ./scripts/run-tests.sh
   \`\`\`

4. **Deploy Locally**
   \`\`\`bash
   ./scripts/deploy-local.sh
   \`\`\`

5. **Start Application**
   \`\`\`bash
   ~/student-evaluator-deploy/start.sh
   \`\`\`

## Application URLs

- **Main Application**: http://localhost:8080
- **H2 Database Console**: http://localhost:8080/h2-console
- **Health Check**: http://localhost:8080/actuator/health

## Directory Structure

\`\`\`
project-root/
├── scripts/           # Build and deployment scripts
├── data/             # Local data storage
│   ├── assignments/  # Assignment files
│   ├── submissions/  # Student submissions
│   └── test-files/   # JUnit test files
├── logs/             # Application logs
└── target/           # Build artifacts
\`\`\`

## Development Workflow

1. Make code changes
2. Run tests: \`./scripts/run-tests.sh --type unit\`
3. Build application: \`./scripts/build.sh\`
4. Deploy locally: \`./scripts/deploy-local.sh\`
5. Test manually or run integration tests

## Troubleshooting

- Check logs in \`logs/application.log\`
- Verify Java and Maven versions
- Ensure all dependencies are downloaded
- Check database connection in H2 console

## Configuration

Local configuration is stored in:
- Development: \`application-local.yml.template\`
- Deployment: \`~/student-evaluator-deploy/config/application-local.yml\`
EOF

echo "✅ Local development README created: LOCAL_DEVELOPMENT.md"

echo ""
echo "=========================================="
echo "Environment Setup Complete!"
echo "=========================================="
echo "✅ Java $JAVA_VERSION detected"
echo "✅ Maven $MAVEN_VERSION detected"
echo "✅ Project directories created"
echo "✅ Dependencies downloaded"
echo "✅ Configuration templates created"
echo ""
echo "Next steps:"
echo "1. Build the application: ./scripts/build.sh"
echo "2. Run tests: ./scripts/run-tests.sh"
echo "3. Deploy locally: ./scripts/deploy-local.sh"
echo ""
echo "For detailed instructions, see: LOCAL_DEVELOPMENT.md"