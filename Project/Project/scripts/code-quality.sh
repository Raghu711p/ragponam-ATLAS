#!/bin/bash

# Student Evaluator System - Code Quality Check Script
# This script runs various code quality checks and generates reports

set -e  # Exit on any error

echo "=========================================="
echo "Student Evaluator System - Code Quality"
echo "=========================================="

# Configuration
REPORTS_DIR="target/quality-reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Create reports directory
mkdir -p "$REPORTS_DIR"

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Run Maven checkstyle (if configured)
echo "Running code style checks..."
if mvn help:describe -Dplugin=org.apache.maven.plugins:maven-checkstyle-plugin -Ddetail=false -q >/dev/null 2>&1; then
    echo "  - Running Checkstyle..."
    mvn checkstyle:check -q || echo "  ⚠️  Checkstyle issues found"
    if [ -f "target/checkstyle-result.xml" ]; then
        cp target/checkstyle-result.xml "$REPORTS_DIR/checkstyle-$TIMESTAMP.xml"
    fi
else
    echo "  - Checkstyle plugin not configured"
fi

# Run Maven PMD (if configured)
echo "Running static analysis..."
if mvn help:describe -Dplugin=org.apache.maven.plugins:maven-pmd-plugin -Ddetail=false -q >/dev/null 2>&1; then
    echo "  - Running PMD..."
    mvn pmd:check -q || echo "  ⚠️  PMD issues found"
    if [ -f "target/pmd.xml" ]; then
        cp target/pmd.xml "$REPORTS_DIR/pmd-$TIMESTAMP.xml"
    fi
else
    echo "  - PMD plugin not configured"
fi

# Run SpotBugs (if configured)
if mvn help:describe -Dplugin=com.github.spotbugs:spotbugs-maven-plugin -Ddetail=false -q >/dev/null 2>&1; then
    echo "  - Running SpotBugs..."
    mvn spotbugs:check -q || echo "  ⚠️  SpotBugs issues found"
    if [ -f "target/spotbugsXml.xml" ]; then
        cp target/spotbugsXml.xml "$REPORTS_DIR/spotbugs-$TIMESTAMP.xml"
    fi
else
    echo "  - SpotBugs plugin not configured"
fi

# Run dependency check
echo "Running dependency analysis..."
mvn dependency:analyze -q > "$REPORTS_DIR/dependency-analysis-$TIMESTAMP.txt" 2>&1 || true
echo "  - Dependency analysis completed"

# Check for common issues in code
echo "Running custom code quality checks..."

# Check for TODO/FIXME comments
echo "  - Checking for TODO/FIXME comments..."
TODO_COUNT=$(find src -name "*.java" -exec grep -l "TODO\|FIXME" {} \; | wc -l)
if [ $TODO_COUNT -gt 0 ]; then
    echo "    Found $TODO_COUNT files with TODO/FIXME comments"
    find src -name "*.java" -exec grep -Hn "TODO\|FIXME" {} \; > "$REPORTS_DIR/todo-fixme-$TIMESTAMP.txt"
else
    echo "    No TODO/FIXME comments found"
fi

# Check for System.out.println usage
echo "  - Checking for System.out.println usage..."
SYSOUT_COUNT=$(find src -name "*.java" -exec grep -l "System\.out\.print" {} \; | wc -l)
if [ $SYSOUT_COUNT -gt 0 ]; then
    echo "    Found $SYSOUT_COUNT files with System.out.println"
    find src -name "*.java" -exec grep -Hn "System\.out\.print" {} \; > "$REPORTS_DIR/system-out-$TIMESTAMP.txt"
else
    echo "    No System.out.println usage found"
fi

# Check for empty catch blocks
echo "  - Checking for empty catch blocks..."
EMPTY_CATCH=$(find src -name "*.java" -exec grep -Pzo "catch\s*\([^)]+\)\s*\{\s*\}" {} \; | grep -c "catch" || true)
if [ $EMPTY_CATCH -gt 0 ]; then
    echo "    Found $EMPTY_CATCH potential empty catch blocks"
else
    echo "    No empty catch blocks found"
fi

# Check test coverage (if JaCoCo is configured)
echo "Running test coverage analysis..."
if mvn help:describe -Dplugin=org.jacoco:jacoco-maven-plugin -Ddetail=false -q >/dev/null 2>&1; then
    echo "  - Running JaCoCo test coverage..."
    mvn clean test jacoco:report -q
    if [ -f "target/site/jacoco/index.html" ]; then
        echo "    Coverage report generated: target/site/jacoco/index.html"
        cp -r target/site/jacoco "$REPORTS_DIR/coverage-$TIMESTAMP"
    fi
else
    echo "  - JaCoCo plugin not configured"
fi

# Generate summary report
echo "Generating quality summary..."
cat > "$REPORTS_DIR/quality-summary-$TIMESTAMP.txt" << EOF
Code Quality Report - $(date)
=====================================

Files Analyzed:
- Java files: $(find src -name "*.java" | wc -l)
- Test files: $(find src/test -name "*.java" | wc -l)
- Resource files: $(find src -name "*.yml" -o -name "*.properties" -o -name "*.xml" | wc -l)

Issues Found:
- TODO/FIXME comments: $TODO_COUNT files
- System.out.println usage: $SYSOUT_COUNT files
- Empty catch blocks: $EMPTY_CATCH potential issues

Reports Generated:
$(ls -la "$REPORTS_DIR" | grep "$TIMESTAMP" | awk '{print "- " $9}')

Recommendations:
1. Review and address TODO/FIXME comments
2. Replace System.out.println with proper logging
3. Handle exceptions properly in catch blocks
4. Run static analysis tools regularly
5. Maintain test coverage above 80%
EOF

echo ""
echo "=========================================="
echo "Code Quality Check Summary"
echo "=========================================="
echo "Java files analyzed: $(find src -name "*.java" | wc -l)"
echo "TODO/FIXME comments: $TODO_COUNT files"
echo "System.out.println usage: $SYSOUT_COUNT files"
echo "Empty catch blocks: $EMPTY_CATCH potential issues"
echo ""
echo "Reports generated in: $REPORTS_DIR/"
echo "Summary report: $REPORTS_DIR/quality-summary-$TIMESTAMP.txt"
echo ""

# Return appropriate exit code
if [ $TODO_COUNT -gt 10 ] || [ $SYSOUT_COUNT -gt 5 ]; then
    echo "⚠️  Code quality issues detected. Review recommended."
    exit 1
else
    echo "✅ Code quality checks completed successfully"
    exit 0
fi