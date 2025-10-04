#!/bin/bash

# Student Evaluator System - Master Deployment Script
# This script orchestrates the complete deployment process

set -e  # Exit on any error

echo "=========================================="
echo "Student Evaluator System - Master Deploy"
echo "=========================================="

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
DEPLOYMENT_TYPE="local"
SKIP_TESTS=false
SKIP_BUILD=false
AUTO_START=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --type)
            DEPLOYMENT_TYPE="$2"
            shift 2
            ;;
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --auto-start)
            AUTO_START=true
            shift
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --type TYPE      Deployment type (local|production)"
            echo "  --skip-tests     Skip running tests"
            echo "  --skip-build     Skip building application"
            echo "  --auto-start     Automatically start application after deployment"
            echo "  --help           Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                              # Full deployment with tests"
            echo "  $0 --skip-tests                # Deploy without running tests"
            echo "  $0 --auto-start                # Deploy and start application"
            echo "  $0 --type production           # Production deployment"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

cd "$PROJECT_ROOT"

echo "Deployment Configuration:"
echo "  Type: $DEPLOYMENT_TYPE"
echo "  Skip Tests: $SKIP_TESTS"
echo "  Skip Build: $SKIP_BUILD"
echo "  Auto Start: $AUTO_START"
echo ""

# Step 1: Environment Setup
echo "Step 1: Environment Setup"
echo "-------------------------"
if [ -f "$SCRIPT_DIR/setup-environment.sh" ]; then
    "$SCRIPT_DIR/setup-environment.sh"
else
    echo "⚠️  Environment setup script not found, skipping..."
fi

# Step 2: Code Quality Checks
echo ""
echo "Step 2: Code Quality Checks"
echo "---------------------------"
if [ -f "$SCRIPT_DIR/code-quality.sh" ]; then
    "$SCRIPT_DIR/code-quality.sh" || echo "⚠️  Code quality issues detected, continuing..."
else
    echo "⚠️  Code quality script not found, skipping..."
fi

# Step 3: Run Tests
if [ "$SKIP_TESTS" = false ]; then
    echo ""
    echo "Step 3: Running Tests"
    echo "--------------------"
    if [ -f "$SCRIPT_DIR/run-tests.sh" ]; then
        "$SCRIPT_DIR/run-tests.sh" --type all
    else
        echo "Running tests with Maven..."
        mvn test
    fi
else
    echo ""
    echo "Step 3: Skipping Tests (--skip-tests specified)"
    echo "-----------------------------------------------"
fi

# Step 4: Build Application
if [ "$SKIP_BUILD" = false ]; then
    echo ""
    echo "Step 4: Building Application"
    echo "---------------------------"
    if [ -f "$SCRIPT_DIR/build.sh" ]; then
        "$SCRIPT_DIR/build.sh"
    else
        echo "Building with Maven..."
        mvn clean package -DskipTests
    fi
else
    echo ""
    echo "Step 4: Skipping Build (--skip-build specified)"
    echo "-----------------------------------------------"
fi

# Step 5: Deploy Application
echo ""
echo "Step 5: Deploying Application"
echo "-----------------------------"
case $DEPLOYMENT_TYPE in
    "local")
        if [ -f "$SCRIPT_DIR/deploy-local.sh" ]; then
            "$SCRIPT_DIR/deploy-local.sh"
        else
            echo "❌ Local deployment script not found"
            exit 1
        fi
        ;;
    "production")
        echo "Production deployment not implemented yet"
        exit 1
        ;;
    *)
        echo "❌ Unknown deployment type: $DEPLOYMENT_TYPE"
        exit 1
        ;;
esac

# Step 6: Start Application (if requested)
if [ "$AUTO_START" = true ]; then
    echo ""
    echo "Step 6: Starting Application"
    echo "---------------------------"
    case $DEPLOYMENT_TYPE in
        "local")
            DEPLOY_DIR="$HOME/student-evaluator-deploy"
            if [ -f "$DEPLOY_DIR/start.sh" ]; then
                echo "Starting application..."
                "$DEPLOY_DIR/start.sh"
                
                # Wait for application to start
                echo "Waiting for application to start..."
                for i in {1..30}; do
                    if curl -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
                        echo "✅ Application started successfully!"
                        echo "   URL: http://localhost:8080"
                        echo "   H2 Console: http://localhost:8080/h2-console"
                        break
                    fi
                    sleep 2
                done
                
                if [ $i -eq 30 ]; then
                    echo "⚠️  Application may not have started properly"
                    echo "   Check logs: $DEPLOY_DIR/logs/"
                fi
            else
                echo "❌ Start script not found: $DEPLOY_DIR/start.sh"
            fi
            ;;
    esac
else
    echo ""
    echo "Step 6: Application Ready for Manual Start"
    echo "-----------------------------------------"
    case $DEPLOYMENT_TYPE in
        "local")
            DEPLOY_DIR="$HOME/student-evaluator-deploy"
            echo "To start the application manually:"
            echo "  $DEPLOY_DIR/start.sh"
            echo ""
            echo "To check status:"
            echo "  $DEPLOY_DIR/status.sh"
            echo ""
            echo "To stop the application:"
            echo "  $DEPLOY_DIR/stop.sh"
            ;;
    esac
fi

echo ""
echo "=========================================="
echo "Deployment Completed Successfully!"
echo "=========================================="
echo "Deployment Type: $DEPLOYMENT_TYPE"
echo "Timestamp: $(date)"

# Generate deployment summary
SUMMARY_FILE="deployment-summary-$(date +%Y%m%d_%H%M%S).txt"
cat > "$SUMMARY_FILE" << EOF
Student Evaluator System - Deployment Summary
=============================================

Deployment Details:
- Type: $DEPLOYMENT_TYPE
- Date: $(date)
- User: $(whoami)
- Host: $(hostname)

Configuration:
- Skip Tests: $SKIP_TESTS
- Skip Build: $SKIP_BUILD
- Auto Start: $AUTO_START

Build Information:
- Java Version: $(java -version 2>&1 | head -n 1)
- Maven Version: $(mvn -version | head -n 1)
- JAR File: target/student-evaluator-system-1.0.0.jar

Deployment Location:
- Directory: $HOME/student-evaluator-deploy
- Configuration: $HOME/student-evaluator-deploy/config/application-local.yml
- Logs: $HOME/student-evaluator-deploy/logs/
- Data: $HOME/student-evaluator-deploy/data/

Application URLs:
- Main Application: http://localhost:8080
- Health Check: http://localhost:8080/actuator/health
- H2 Console: http://localhost:8080/h2-console

Management Commands:
- Start: $HOME/student-evaluator-deploy/start.sh
- Stop: $HOME/student-evaluator-deploy/stop.sh
- Status: $HOME/student-evaluator-deploy/status.sh

Notes:
- Application uses H2 database for local storage
- All data is stored locally in the deployment directory
- No containerization is used as requested
- Logs are rotated automatically
EOF

echo "Deployment summary saved to: $SUMMARY_FILE"