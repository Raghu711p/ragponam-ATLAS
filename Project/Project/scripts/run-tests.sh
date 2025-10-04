#!/bin/bash

# Student Evaluator System - Test Automation Script
# This script runs different types of tests with proper reporting

set -e  # Exit on any error

echo "=========================================="
echo "Student Evaluator System - Test Runner"
echo "=========================================="

# Configuration
REPORTS_DIR="target/test-reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Create reports directory
mkdir -p "$REPORTS_DIR"

# Function to run tests with specific profile
run_test_suite() {
    local test_type=$1
    local test_pattern=$2
    local description=$3
    
    echo ""
    echo "Running $description..."
    echo "----------------------------------------"
    
    if [ -n "$test_pattern" ]; then
        mvn test -Dtest="$test_pattern" -Dmaven.test.failure.ignore=true
    else
        case $test_type in
            "unit")
                mvn test -Dtest="!*IntegrationTest,!*IT,!*BDD*" -Dmaven.test.failure.ignore=true
                ;;
            "integration")
                mvn test -Dtest="*IntegrationTest,*IT" -Dmaven.test.failure.ignore=true
                ;;
            "bdd")
                mvn test -Dtest="*CucumberTestRunner" -Dmaven.test.failure.ignore=true
                ;;
            "all")
                mvn test -Dmaven.test.failure.ignore=true
                ;;
        esac
    fi
    
    # Copy test results
    if [ -d "target/surefire-reports" ]; then
        cp -r target/surefire-reports "$REPORTS_DIR/${test_type}_${TIMESTAMP}"
    fi
}

# Parse command line arguments
TEST_TYPE="all"
SPECIFIC_TEST=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --type)
            TEST_TYPE="$2"
            shift 2
            ;;
        --test)
            SPECIFIC_TEST="$2"
            shift 2
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --type TYPE     Test type to run (unit|integration|bdd|all)"
            echo "  --test PATTERN  Specific test pattern to run"
            echo "  --help          Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                           # Run all tests"
            echo "  $0 --type unit              # Run only unit tests"
            echo "  $0 --type integration       # Run only integration tests"
            echo "  $0 --type bdd               # Run only BDD tests"
            echo "  $0 --test '*Service*'       # Run tests matching pattern"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Clean previous test results
echo "Cleaning previous test results..."
mvn clean

# Run tests based on type
case $TEST_TYPE in
    "unit")
        run_test_suite "unit" "" "Unit Tests"
        ;;
    "integration")
        run_test_suite "integration" "" "Integration Tests"
        ;;
    "bdd")
        run_test_suite "bdd" "" "BDD Tests"
        ;;
    "all")
        run_test_suite "unit" "" "Unit Tests"
        run_test_suite "integration" "" "Integration Tests"
        run_test_suite "bdd" "" "BDD Tests"
        ;;
    *)
        if [ -n "$SPECIFIC_TEST" ]; then
            run_test_suite "custom" "$SPECIFIC_TEST" "Custom Test Pattern: $SPECIFIC_TEST"
        else
            echo "Invalid test type: $TEST_TYPE"
            echo "Valid types: unit, integration, bdd, all"
            exit 1
        fi
        ;;
esac

# Generate test summary
echo ""
echo "=========================================="
echo "Test Execution Summary"
echo "=========================================="

# Count test results
if [ -d "target/surefire-reports" ]; then
    TOTAL_TESTS=$(find target/surefire-reports -name "TEST-*.xml" -exec grep -h "tests=" {} \; | sed 's/.*tests="\([0-9]*\)".*/\1/' | awk '{sum += $1} END {print sum}')
    FAILED_TESTS=$(find target/surefire-reports -name "TEST-*.xml" -exec grep -h "failures=" {} \; | sed 's/.*failures="\([0-9]*\)".*/\1/' | awk '{sum += $1} END {print sum}')
    ERROR_TESTS=$(find target/surefire-reports -name "TEST-*.xml" -exec grep -h "errors=" {} \; | sed 's/.*errors="\([0-9]*\)".*/\1/' | awk '{sum += $1} END {print sum}')
    SKIPPED_TESTS=$(find target/surefire-reports -name "TEST-*.xml" -exec grep -h "skipped=" {} \; | sed 's/.*skipped="\([0-9]*\)".*/\1/' | awk '{sum += $1} END {print sum}')
    
    PASSED_TESTS=$((TOTAL_TESTS - FAILED_TESTS - ERROR_TESTS - SKIPPED_TESTS))
    
    echo "Total Tests:   $TOTAL_TESTS"
    echo "Passed Tests:  $PASSED_TESTS"
    echo "Failed Tests:  $FAILED_TESTS"
    echo "Error Tests:   $ERROR_TESTS"
    echo "Skipped Tests: $SKIPPED_TESTS"
    
    if [ $FAILED_TESTS -eq 0 ] && [ $ERROR_TESTS -eq 0 ]; then
        echo ""
        echo "✅ All tests passed successfully!"
    else
        echo ""
        echo "❌ Some tests failed. Check the reports for details."
    fi
else
    echo "No test results found."
fi

# Show report locations
echo ""
echo "Test reports available at:"
echo "  HTML Report: target/surefire-reports/"
echo "  Archived Reports: $REPORTS_DIR/"

# BDD specific reports
if [ "$TEST_TYPE" = "bdd" ] || [ "$TEST_TYPE" = "all" ]; then
    if [ -d "target/cucumber-reports" ]; then
        echo "  BDD Reports: target/cucumber-reports/"
    fi
fi

echo ""
echo "Test execution completed at: $(date)"