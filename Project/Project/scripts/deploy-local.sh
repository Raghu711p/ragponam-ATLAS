#!/bin/bash

# Student Evaluator System - Local Deployment Script
# This script deploys the application to local environment

set -e  # Exit on any error

echo "=========================================="
echo "Student Evaluator System - Local Deploy"
echo "=========================================="

# Configuration
APP_NAME="student-evaluator-system"
APP_VERSION="1.0.0"
JAR_FILE="target/${APP_NAME}-${APP_VERSION}.jar"
DEPLOY_DIR="$HOME/student-evaluator-deploy"
LOG_DIR="$DEPLOY_DIR/logs"
DATA_DIR="$DEPLOY_DIR/data"
CONFIG_DIR="$DEPLOY_DIR/config"
PID_FILE="$DEPLOY_DIR/${APP_NAME}.pid"

# Create deployment directories
echo "Creating deployment directories..."
mkdir -p "$DEPLOY_DIR"
mkdir -p "$LOG_DIR"
mkdir -p "$DATA_DIR"
mkdir -p "$CONFIG_DIR"

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found: $JAR_FILE"
    echo "Please run build script first"
    exit 1
fi

# Stop existing application if running
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null 2>&1; then
        echo "Stopping existing application (PID: $PID)..."
        kill $PID
        sleep 5
        
        # Force kill if still running
        if ps -p $PID > /dev/null 2>&1; then
            echo "Force killing application..."
            kill -9 $PID
        fi
    fi
    rm -f "$PID_FILE"
fi

# Copy JAR file to deployment directory
echo "Copying application JAR..."
cp "$JAR_FILE" "$DEPLOY_DIR/"

# Create application configuration
echo "Creating application configuration..."
cat > "$CONFIG_DIR/application-local.yml" << EOF
server:
  port: 8080
  servlet:
    context-path: /

spring:
  application:
    name: student-evaluator-system
  
  datasource:
    url: jdbc:h2:file:${DATA_DIR}/studentevaluator;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
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
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        
logging:
  level:
    com.studentevaluator: INFO
    org.springframework: WARN
  file:
    name: ${LOG_DIR}/application.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    console: "%d{HH:mm:ss} %-5level %logger{36} - %msg%n"

# Application specific settings
app:
  storage:
    base-path: ${DATA_DIR}
    assignments-path: ${DATA_DIR}/assignments
    submissions-path: ${DATA_DIR}/submissions
    test-files-path: ${DATA_DIR}/test-files
  
  evaluation:
    timeout-seconds: 30
    max-concurrent-evaluations: 5
    
  security:
    max-file-size: 1048576  # 1MB
    allowed-extensions: [".java"]
EOF

# Create startup script
echo "Creating startup script..."
cat > "$DEPLOY_DIR/start.sh" << EOF
#!/bin/bash

cd "$DEPLOY_DIR"

# Set JVM options
export JAVA_OPTS="-Xms512m -Xmx1024m -Dspring.profiles.active=local -Dspring.config.location=file:$CONFIG_DIR/application-local.yml"

# Start the application
echo "Starting Student Evaluator System..."
nohup java \$JAVA_OPTS -jar ${APP_NAME}-${APP_VERSION}.jar > $LOG_DIR/startup.log 2>&1 &

# Save PID
echo \$! > $PID_FILE

echo "Application started with PID: \$(cat $PID_FILE)"
echo "Logs available at: $LOG_DIR/"
echo "Application will be available at: http://localhost:8080"
echo "H2 Console available at: http://localhost:8080/h2-console"
EOF

chmod +x "$DEPLOY_DIR/start.sh"

# Create stop script
echo "Creating stop script..."
cat > "$DEPLOY_DIR/stop.sh" << EOF
#!/bin/bash

if [ -f "$PID_FILE" ]; then
    PID=\$(cat "$PID_FILE")
    if ps -p \$PID > /dev/null 2>&1; then
        echo "Stopping Student Evaluator System (PID: \$PID)..."
        kill \$PID
        sleep 5
        
        # Check if process is still running
        if ps -p \$PID > /dev/null 2>&1; then
            echo "Force killing application..."
            kill -9 \$PID
        fi
        
        echo "Application stopped"
    else
        echo "Application is not running"
    fi
    rm -f "$PID_FILE"
else
    echo "PID file not found. Application may not be running."
fi
EOF

chmod +x "$DEPLOY_DIR/stop.sh"

# Create status script
echo "Creating status script..."
cat > "$DEPLOY_DIR/status.sh" << EOF
#!/bin/bash

if [ -f "$PID_FILE" ]; then
    PID=\$(cat "$PID_FILE")
    if ps -p \$PID > /dev/null 2>&1; then
        echo "Student Evaluator System is running (PID: \$PID)"
        echo "Application URL: http://localhost:8080"
        echo "H2 Console URL: http://localhost:8080/h2-console"
        echo "Log file: $LOG_DIR/application.log"
    else
        echo "Student Evaluator System is not running (stale PID file)"
        rm -f "$PID_FILE"
    fi
else
    echo "Student Evaluator System is not running"
fi
EOF

chmod +x "$DEPLOY_DIR/status.sh"

# Create data directories
echo "Creating data directories..."
mkdir -p "$DATA_DIR/assignments"
mkdir -p "$DATA_DIR/submissions"
mkdir -p "$DATA_DIR/test-files"

echo "=========================================="
echo "Deployment completed successfully!"
echo "=========================================="
echo "Deployment directory: $DEPLOY_DIR"
echo "Configuration file: $CONFIG_DIR/application-local.yml"
echo "Log directory: $LOG_DIR"
echo "Data directory: $DATA_DIR"
echo ""
echo "To start the application:"
echo "  $DEPLOY_DIR/start.sh"
echo ""
echo "To stop the application:"
echo "  $DEPLOY_DIR/stop.sh"
echo ""
echo "To check status:"
echo "  $DEPLOY_DIR/status.sh"