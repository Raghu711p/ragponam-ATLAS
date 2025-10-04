# Student Evaluator System - CLI User Guide

## Overview

The Student Evaluator System provides a command-line interface (CLI) for interacting with the evaluation system. The CLI offers all the functionality available through the REST API in a convenient command-line format.

## Installation

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Access to the Student Evaluator System API (running locally or remotely)

### Building the CLI

```bash
# Clone the repository
git clone https://github.com/your-org/student-evaluator-system.git
cd student-evaluator-system

# Build the project
mvn clean package

# The CLI jar will be available at:
# target/student-evaluator-cli-1.0.0.jar
```

### Creating an Alias (Optional)

For convenience, create an alias:

```bash
# Add to your ~/.bashrc or ~/.zshrc
alias evaluator='java -jar /path/to/student-evaluator-cli-1.0.0.jar'

# Reload your shell configuration
source ~/.bashrc
```

## Configuration

### API Endpoint Configuration

The CLI connects to the REST API. Configure the endpoint:

```bash
# Set environment variable (recommended)
export EVALUATOR_API_URL=http://localhost:8080/api/v1

# Or use the --api-url flag with each command
evaluator --api-url http://localhost:8080/api/v1 submit --help
```

### Default Configuration File

Create a configuration file at `~/.evaluator/config.properties`:

```properties
# API Configuration
api.url=http://localhost:8080/api/v1
api.timeout=30000

# Default Settings
default.student.id=your-student-id
default.output.format=json

# File Upload Settings
max.file.size=1048576
allowed.extensions=.java
```

## Commands Overview

The CLI provides the following main commands:

- `submit` - Submit assignment files for evaluation
- `upload-tests` - Upload test files for assignments
- `evaluate` - Trigger evaluation process
- `results` - Retrieve evaluation results
- `list` - List assignments or evaluations
- `status` - Check evaluation status

## Command Reference

### Global Options

All commands support these global options:

```bash
--api-url <url>     # API endpoint URL
--timeout <ms>      # Request timeout in milliseconds
--format <format>   # Output format: json, table, csv
--verbose           # Enable verbose output
--help              # Show help information
```

### submit - Submit Assignment Files

Submit a student's Java file for evaluation.

#### Syntax
```bash
evaluator submit [OPTIONS] <file>
```

#### Options
- `--student <id>` - Student identifier (required)
- `--assignment <id>` - Assignment identifier (required)
- `--wait` - Wait for evaluation to complete
- `--output <file>` - Save results to file

#### Examples

```bash
# Basic submission
evaluator submit --student student123 --assignment calc-assignment-1 Calculator.java

# Submit and wait for results
evaluator submit --student student123 --assignment calc-assignment-1 --wait Calculator.java

# Submit with custom API URL
evaluator --api-url http://prod.example.com/api/v1 submit --student student123 --assignment calc-assignment-1 Calculator.java

# Submit and save results to file
evaluator submit --student student123 --assignment calc-assignment-1 --output results.json Calculator.java
```

#### Output

**Success:**
```json
{
  "evaluationId": "eval-123-456",
  "studentId": "student123",
  "assignmentId": "calc-assignment-1",
  "status": "PENDING",
  "message": "Evaluation started successfully"
}
```

**With --wait flag:**
```json
{
  "evaluationId": "eval-123-456",
  "studentId": "student123",
  "assignmentId": "calc-assignment-1",
  "status": "COMPLETED",
  "score": 85.5,
  "maxScore": 100.0,
  "compilationResult": {
    "successful": true,
    "output": "Compilation successful"
  },
  "testResult": {
    "totalTests": 10,
    "passedTests": 8,
    "failedTests": 2
  }
}
```

### upload-tests - Upload Test Files

Upload JUnit test files for an assignment.

#### Syntax
```bash
evaluator upload-tests [OPTIONS] --assignment <id> <test-files...>
```

#### Options
- `--assignment <id>` - Assignment identifier (required)
- `--validate` - Validate test files before upload

#### Examples

```bash
# Upload single test file
evaluator upload-tests --assignment calc-assignment-1 CalculatorTest.java

# Upload multiple test files
evaluator upload-tests --assignment calc-assignment-1 CalculatorTest.java AdvancedCalculatorTest.java

# Upload with validation
evaluator upload-tests --assignment calc-assignment-1 --validate CalculatorTest.java
```

#### Output

```json
{
  "assignmentId": "calc-assignment-1",
  "uploadedFiles": [
    "CalculatorTest.java",
    "AdvancedCalculatorTest.java"
  ],
  "testFilePath": "/tests/calc-assignment-1/",
  "message": "Test files uploaded successfully"
}
```

### evaluate - Trigger Evaluation

Trigger evaluation for a previously submitted assignment.

#### Syntax
```bash
evaluator evaluate [OPTIONS]
```

#### Options
- `--evaluation <id>` - Evaluation identifier (required)
- `--wait` - Wait for evaluation to complete
- `--timeout <seconds>` - Maximum wait time (default: 300)

#### Examples

```bash
# Trigger evaluation
evaluator evaluate --evaluation eval-123-456

# Trigger and wait for completion
evaluator evaluate --evaluation eval-123-456 --wait

# Trigger with custom timeout
evaluator evaluate --evaluation eval-123-456 --wait --timeout 600
```

### results - Retrieve Evaluation Results

Get detailed evaluation results.

#### Syntax
```bash
evaluator results [OPTIONS] <evaluation-id>
```

#### Options
- `--detailed` - Include detailed test case information
- `--logs` - Include execution logs
- `--output <file>` - Save results to file
- `--format <format>` - Output format: json, table, summary

#### Examples

```bash
# Get basic results
evaluator results eval-123-456

# Get detailed results with logs
evaluator results --detailed --logs eval-123-456

# Get results in table format
evaluator --format table results eval-123-456

# Save results to file
evaluator results --output evaluation-report.json eval-123-456
```

#### Output Formats

**JSON Format (default):**
```json
{
  "evaluationId": "eval-123-456",
  "studentId": "student123",
  "assignmentId": "calc-assignment-1",
  "finalScore": 85.5,
  "maxScore": 100.0,
  "status": "COMPLETED",
  "compilationResult": {
    "successful": true,
    "output": "Compilation successful"
  },
  "testResult": {
    "totalTests": 10,
    "passedTests": 8,
    "failedTests": 2,
    "testCases": [...]
  }
}
```

**Table Format:**
```
┌─────────────────┬──────────────────────────────────────┐
│ Field           │ Value                                │
├─────────────────┼──────────────────────────────────────┤
│ Evaluation ID   │ eval-123-456                         │
│ Student ID      │ student123                           │
│ Assignment ID   │ calc-assignment-1                    │
│ Status          │ COMPLETED                            │
│ Score           │ 85.5 / 100.0                        │
│ Compilation     │ ✓ Successful                         │
│ Tests Passed    │ 8 / 10                               │
│ Tests Failed    │ 2                                    │
│ Evaluated At    │ 2024-01-15T10:35:00Z                 │
└─────────────────┴──────────────────────────────────────┘
```

**Summary Format:**
```
Evaluation Summary
==================
Student: student123
Assignment: calc-assignment-1
Score: 85.5/100.0 (85.5%)

Compilation: ✓ PASSED
Tests: 8/10 PASSED (80.0%)

Failed Tests:
- testDivisionByZero: Expected ArithmeticException was not thrown
- testInvalidInput: Input validation failed
```

### status - Check Evaluation Status

Check the current status of an evaluation.

#### Syntax
```bash
evaluator status [OPTIONS] <evaluation-id>
```

#### Options
- `--watch` - Continuously monitor status until completion
- `--interval <seconds>` - Polling interval for watch mode (default: 5)

#### Examples

```bash
# Check status once
evaluator status eval-123-456

# Monitor status continuously
evaluator status --watch eval-123-456

# Monitor with custom interval
evaluator status --watch --interval 10 eval-123-456
```

#### Output

```json
{
  "evaluationId": "eval-123-456",
  "status": "PENDING",
  "progress": {
    "stage": "COMPILATION",
    "percentage": 25,
    "message": "Compiling Java source code..."
  },
  "estimatedCompletion": "2024-01-15T10:32:00Z"
}
```

### list - List Resources

List assignments, evaluations, or students.

#### Syntax
```bash
evaluator list [OPTIONS] <resource-type>
```

#### Resource Types
- `assignments` - List all assignments
- `evaluations` - List evaluations (requires --student)
- `students` - List students (admin only)

#### Options
- `--student <id>` - Filter by student ID
- `--assignment <id>` - Filter by assignment ID
- `--status <status>` - Filter by status (PENDING, COMPLETED, FAILED)
- `--limit <n>` - Limit number of results
- `--page <n>` - Page number for pagination

#### Examples

```bash
# List all assignments
evaluator list assignments

# List evaluations for a student
evaluator list evaluations --student student123

# List recent evaluations
evaluator list evaluations --student student123 --limit 5

# List failed evaluations
evaluator list evaluations --student student123 --status FAILED
```

## Advanced Usage

### Batch Operations

Process multiple files using shell scripting:

```bash
#!/bin/bash

STUDENT_ID="student123"
ASSIGNMENT_ID="calc-assignment-1"

# Submit multiple student files
for file in submissions/*.java; do
  echo "Processing $file..."
  
  # Extract student ID from filename (assuming format: studentID_Assignment.java)
  CURRENT_STUDENT=$(basename "$file" | cut -d'_' -f1)
  
  # Submit and wait for results
  RESULT=$(evaluator submit --student "$CURRENT_STUDENT" --assignment "$ASSIGNMENT_ID" --wait "$file")
  
  # Extract score and log it
  SCORE=$(echo "$RESULT" | jq -r '.score')
  echo "$CURRENT_STUDENT: $SCORE" >> scores.txt
  
  # Save detailed results
  echo "$RESULT" > "results/${CURRENT_STUDENT}_results.json"
done

echo "Batch processing completed. Results saved to results/ directory."
```

### Configuration Management

Use different configurations for different environments:

```bash
# Development environment
export EVALUATOR_CONFIG=~/.evaluator/dev-config.properties
evaluator submit --student student123 --assignment test-1 Calculator.java

# Production environment
export EVALUATOR_CONFIG=~/.evaluator/prod-config.properties
evaluator submit --student student123 --assignment assignment-1 Calculator.java
```

### Output Processing

Process CLI output with other tools:

```bash
# Get scores for all students and calculate average
evaluator list evaluations --assignment calc-assignment-1 --format json | \
  jq -r '.[] | select(.status == "COMPLETED") | .score' | \
  awk '{sum+=$1; count++} END {print "Average:", sum/count}'

# Generate CSV report
evaluator list evaluations --assignment calc-assignment-1 --format json | \
  jq -r '.[] | [.studentId, .score, .status, .evaluatedAt] | @csv' > report.csv

# Find students who need help (score < 70)
evaluator list evaluations --assignment calc-assignment-1 --format json | \
  jq -r '.[] | select(.score < 70) | .studentId' > students_need_help.txt
```

## Error Handling

### Common Errors and Solutions

**Connection Error:**
```
Error: Unable to connect to API at http://localhost:8080/api/v1
Solution: Ensure the API server is running and accessible
```

**File Not Found:**
```
Error: File 'Calculator.java' not found
Solution: Check file path and ensure file exists
```

**Invalid Assignment:**
```
Error: Assignment 'invalid-id' not found
Solution: Use 'evaluator list assignments' to see available assignments
```

**Authentication Error:**
```
Error: Unauthorized access
Solution: Check API key configuration or contact administrator
```

### Debugging

Enable verbose output for troubleshooting:

```bash
# Enable verbose mode
evaluator --verbose submit --student student123 --assignment calc-assignment-1 Calculator.java

# Enable debug logging
export EVALUATOR_LOG_LEVEL=DEBUG
evaluator submit --student student123 --assignment calc-assignment-1 Calculator.java
```

## Integration Examples

### IDE Integration

#### VS Code Task Configuration

Create `.vscode/tasks.json`:

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Submit Assignment",
      "type": "shell",
      "command": "evaluator",
      "args": [
        "submit",
        "--student", "${input:studentId}",
        "--assignment", "${input:assignmentId}",
        "--wait",
        "${file}"
      ],
      "group": "build",
      "presentation": {
        "echo": true,
        "reveal": "always",
        "focus": false,
        "panel": "shared"
      }
    }
  ],
  "inputs": [
    {
      "id": "studentId",
      "description": "Student ID",
      "default": "student123",
      "type": "promptString"
    },
    {
      "id": "assignmentId",
      "description": "Assignment ID",
      "default": "calc-assignment-1",
      "type": "promptString"
    }
  ]
}
```

#### IntelliJ IDEA External Tool

1. Go to File → Settings → Tools → External Tools
2. Click "+" to add new tool
3. Configure:
   - Name: Submit Assignment
   - Program: java
   - Arguments: -jar /path/to/student-evaluator-cli-1.0.0.jar submit --student $Prompt$ --assignment $Prompt$ --wait $FilePath$
   - Working directory: $ProjectFileDir$

### CI/CD Integration

#### GitHub Actions

Create `.github/workflows/evaluate.yml`:

```yaml
name: Evaluate Assignment

on:
  push:
    paths:
      - 'assignments/**/*.java'

jobs:
  evaluate:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up Java
      uses: actions/setup-java@v2
      with:
        java-version: '11'
        distribution: 'adopt'
    
    - name: Download CLI
      run: |
        wget https://github.com/your-org/student-evaluator-system/releases/latest/download/student-evaluator-cli.jar
        chmod +x student-evaluator-cli.jar
    
    - name: Submit assignments
      run: |
        for file in assignments/**/*.java; do
          student_id=$(basename "$file" .java)
          java -jar student-evaluator-cli.jar submit \
            --api-url ${{ secrets.EVALUATOR_API_URL }} \
            --student "$student_id" \
            --assignment "github-assignment" \
            --wait \
            "$file"
        done
```

## Best Practices

### File Organization

```
project/
├── assignments/
│   ├── assignment-1/
│   │   ├── Calculator.java          # Assignment template
│   │   └── tests/
│   │       └── CalculatorTest.java  # Test files
│   └── assignment-2/
├── submissions/
│   ├── student123_Calculator.java   # Student submissions
│   └── student456_Calculator.java
├── results/
│   ├── student123_results.json      # Evaluation results
│   └── student456_results.json
└── scripts/
    ├── submit-all.sh               # Batch submission script
    └── generate-report.sh          # Report generation script
```

### Workflow Recommendations

1. **Setup Phase:**
   ```bash
   # Upload assignment template
   evaluator upload-assignment --title "Calculator" Calculator.java
   
   # Upload test files
   evaluator upload-tests --assignment calc-assignment-1 CalculatorTest.java
   ```

2. **Submission Phase:**
   ```bash
   # Students submit their work
   evaluator submit --student student123 --assignment calc-assignment-1 MyCalculator.java
   ```

3. **Evaluation Phase:**
   ```bash
   # Check status and get results
   evaluator status eval-123-456
   evaluator results --detailed eval-123-456
   ```

4. **Reporting Phase:**
   ```bash
   # Generate reports
   evaluator list evaluations --assignment calc-assignment-1 --format csv > report.csv
   ```

## Troubleshooting

### Performance Issues

- Use `--timeout` to increase request timeout for large files
- Process files in smaller batches for bulk operations
- Monitor API server resources during heavy usage

### Network Issues

- Check firewall settings and network connectivity
- Use `--verbose` flag to see detailed HTTP requests
- Verify API endpoint URL and port

### File Issues

- Ensure files are valid Java source code
- Check file permissions and accessibility
- Verify file size limits (default: 1MB)

## Support and Resources

- **CLI Help**: Use `evaluator --help` or `evaluator <command> --help`
- **API Documentation**: See [REST API Guide](REST_API_GUIDE.md)
- **Issue Reporting**: Submit issues on the project repository
- **Community**: Join the discussion forum for tips and best practices

## Changelog

### Version 1.0.0
- Initial CLI release
- All core commands implemented
- Configuration file support
- Multiple output formats
- Batch processing capabilities