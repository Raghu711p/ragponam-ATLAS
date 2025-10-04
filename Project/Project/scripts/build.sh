#!/bin/bash

# Student Evaluator System - Local Build Script
# This script builds the application locally without containerization

set -e  # Exit on any error

echo "=========================================="
echo "Student Evaluator System - Local Build"
echo "=========================================="

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

# Display Java and Maven versions
echo "Java version:"
java -version
echo ""
echo "Maven version:"
mvn -version
echo ""

# Clean previous builds
echo "Cleaning previous builds..."
mvn clean

# Compile the application
echo "Compiling application..."
mvn compile

# Run tests
echo "Running tests..."
mvn test

# Package the application
echo "Packaging application..."
mvn package -DskipTests

# Check if JAR was created successfully
JAR_FILE="target/student-evaluator-system-1.0.0.jar"
if [ -f "$JAR_FILE" ]; then
    echo "Build successful! JAR file created: $JAR_FILE"
    echo "File size: $(du -h $JAR_FILE | cut -f1)"
else
    echo "Error: JAR file not found after build"
    exit 1
fi

echo "=========================================="
echo "Build completed successfully!"
echo "=========================================="