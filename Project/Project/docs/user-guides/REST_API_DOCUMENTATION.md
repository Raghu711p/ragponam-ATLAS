# Student Evaluator System - REST API User Guide

## Overview

The Student Evaluator System provides a RESTful API for automated evaluation of Java programming assignments. This guide covers how to use the API endpoints to upload assignments, manage test cases, trigger evaluations, and retrieve results.

## Base URL

- **Local Development**: `http://localhost:8080/api/v1`
- **Production**: `https://api.studentevaluator.com/v1`

## Authentication

Currently, the API does not require authentication. Future versions will include API key authentication.

## Content Types

- **Request**: `multipart/form-data` for file uploads, `application/json` for data
- **Response**: `application/json`

## Getting Started

### 1. Upload an Assignment

First, upload a Java assignment file that students will work on:

```bash
curl -X POST http://localhost:8080/api/v1/assignments \
  -F "file=@Calculator.java" \
  -F "title=Calculator Assignment" \
  -F "description=Implement basic calculator operations"
```

**Response:**
```json
{
  "assignmentId": "calc-assignment-1",
  "title": "Calculator Assignment",
  "description": "Implement basic calculator operations",
  "createdAt": "2024-01-15T10:30:00Z",
  "testFilePath": null
}
```

### 2. Upload Test Files

Upload JUnit test files for the assignment:

```bash
curl -X POST http://localhost:8080/api/v1/assignments/calc-assignment-1/tests \
  -F "files=@CalculatorTest.java" \
  -F "files=@AdvancedCalculatorTest.java"
```

**Response:**
```json
{
  "assignmentId": "calc-assignment-1",
  "title": "Calculator Assignment",
  "description": "Implement basic calculator operations",
  "createdAt": "2024-01-15T10:30:00Z",
  "testFilePath": "/tests/calc-assignment-1/"
}
```

### 3. Submit Student Work for Evaluation

Submit a student's Java file for evaluation:

```bash
curl -X POST http://localhost:8080/api/v1/evaluations \
  -F "file=@StudentCalculator.java" \
  -F "studentId=student123" \
  -F "assignmentId=calc-assignment-1"
```

**Response:**
```json
{
  "evaluationId": "eval-123-456",
  "studentId": "student123",
  "assignmentId": "calc-assignment-1",
  "status": "PENDING",
  "score": null,
  "maxScore": null,
  "evaluatedAt": null
}
```

### 4. Check Evaluation Status

Monitor the evaluation progress:

```bash
curl http://localhost:8080/api/v1/evaluations/eval-123-456
```

**Response (In Progress):**
```json
{
  "evaluationId": "eval-123-456",
  "studentId": "student123",
  "assignmentId": "calc-assignment-1",
  "status": "PENDING",
  "score": null,
  "maxScore": null,
  "evaluatedAt": null
}
```

**Response (Completed):**
```json
{
  "evaluationId": "eval-123-456",
  "studentId": "student123",
  "assignmentId": "calc-assignment-1",
  "status": "COMPLETED",
  "score": 85.5,
  "maxScore": 100.0,
  "evaluatedAt": "2024-01-15T10:35:00Z"
}
```

### 5. Get Detailed Results

Retrieve detailed evaluation results including compilation and test information:

```bash
curl http://localhost:8080/api/v1/evaluations/eval-123-456/results
```

**Response:**
```json
{
  "evaluationId": "eval-123-456",
  "compilationResult": {
    "successful": true,
    "output": "Compilation successful",
    "errors": [],
    "compiledClassPath": "/tmp/evaluations/eval-123-456/StudentCalculator.class"
  },
  "testResult": {
    "totalTests": 10,
    "passedTests": 8,
    "failedTests": 2,
    "testCases": [
      {
        "testName": "testAddition",
        "className": "CalculatorTest",
        "status": "PASSED",
        "executionTime": 15,
        "errorMessage": null,
        "stackTrace": null
      },
      {
        "testName": "testDivisionByZero",
        "className": "CalculatorTest",
        "status": "FAILED",
        "executionTime": 8,
        "errorMessage": "Expected ArithmeticException was not thrown",
        "stackTrace": "java.lang.AssertionError: Expected ArithmeticException..."
      }
    ],
    "executionLog": "Running tests for StudentCalculator..."
  },
  "finalScore": 85.5,
  "status": "COMPLETED",
  "evaluatedAt": "2024-01-15T10:35:00Z"
}
```

## API Endpoints Reference

### Assignments

#### List All Assignments
```http
GET /assignments?page=0&size=20
```

#### Upload Assignment
```http
POST /assignments
Content-Type: multipart/form-data

file: (binary)
title: string
description: string (optional)
```

#### Upload Test Files
```http
POST /assignments/{assignmentId}/tests
Content-Type: multipart/form-data

files: (binary array)
```

### Evaluations

#### Trigger Evaluation
```http
POST /evaluations
Content-Type: multipart/form-data

file: (binary)
studentId: string
assignmentId: string
```

#### Get Evaluation Status
```http
GET /evaluations/{evaluationId}
```

#### Get Detailed Results
```http
GET /evaluations/{evaluationId}/results
```

### Students

#### Get Student Scores
```http
GET /students/{studentId}/scores
```

#### Get Student Evaluation History
```http
GET /students/{studentId}/evaluations?assignmentId={assignmentId}&status={status}
```

## Error Handling

The API returns structured error responses with appropriate HTTP status codes:

### Common Error Responses

**400 Bad Request:**
```json
{
  "error": {
    "code": "INVALID_FILE_FORMAT",
    "message": "Only .java files are accepted",
    "details": {
      "fileName": "document.txt",
      "expectedFormat": ".java"
    },
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

**404 Not Found:**
```json
{
  "error": {
    "code": "ASSIGNMENT_NOT_FOUND",
    "message": "Assignment with ID 'invalid-id' not found",
    "details": {
      "assignmentId": "invalid-id"
    },
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

**413 Payload Too Large:**
```json
{
  "error": {
    "code": "FILE_TOO_LARGE",
    "message": "File size exceeds maximum limit of 1MB",
    "details": {
      "fileSize": "2.5MB",
      "maxSize": "1MB"
    },
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

**500 Internal Server Error:**
```json
{
  "error": {
    "code": "COMPILATION_FAILED",
    "message": "Java compilation failed",
    "details": {
      "file": "StudentSubmission.java",
      "line": 15,
      "errors": ["cannot find symbol: variable x"]
    },
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

## Rate Limiting

The API implements rate limiting to prevent abuse:

- **Rate Limit**: 100 requests per minute per IP address
- **Headers**: 
  - `X-RateLimit-Limit`: Maximum requests per window
  - `X-RateLimit-Remaining`: Remaining requests in current window
  - `X-RateLimit-Reset`: Time when the rate limit resets

When rate limit is exceeded:
```json
{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Too many requests. Please try again later.",
    "details": {
      "retryAfter": 60
    },
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

## Best Practices

### File Upload Guidelines

1. **File Size**: Keep files under 1MB
2. **File Format**: Only `.java` files are accepted
3. **File Names**: Use descriptive names without special characters
4. **Content**: Ensure files contain valid Java code

### Evaluation Workflow

1. **Upload Assignment**: Create the assignment first
2. **Upload Tests**: Associate test files with the assignment
3. **Submit Student Work**: Submit student files for evaluation
4. **Poll Status**: Check evaluation status periodically
5. **Retrieve Results**: Get detailed results once completed

### Error Handling

1. **Check Status Codes**: Always check HTTP status codes
2. **Parse Error Messages**: Use error codes for programmatic handling
3. **Retry Logic**: Implement exponential backoff for retries
4. **Validation**: Validate files before uploading

## Examples

### Complete Workflow Example

```bash
#!/bin/bash

# 1. Upload assignment
ASSIGNMENT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/assignments \
  -F "file=@Calculator.java" \
  -F "title=Calculator Assignment" \
  -F "description=Basic calculator operations")

ASSIGNMENT_ID=$(echo $ASSIGNMENT_RESPONSE | jq -r '.assignmentId')
echo "Created assignment: $ASSIGNMENT_ID"

# 2. Upload test files
curl -s -X POST http://localhost:8080/api/v1/assignments/$ASSIGNMENT_ID/tests \
  -F "files=@CalculatorTest.java"

echo "Uploaded test files"

# 3. Submit student work
EVALUATION_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/evaluations \
  -F "file=@StudentCalculator.java" \
  -F "studentId=student123" \
  -F "assignmentId=$ASSIGNMENT_ID")

EVALUATION_ID=$(echo $EVALUATION_RESPONSE | jq -r '.evaluationId')
echo "Started evaluation: $EVALUATION_ID"

# 4. Wait for completion
while true; do
  STATUS_RESPONSE=$(curl -s http://localhost:8080/api/v1/evaluations/$EVALUATION_ID)
  STATUS=$(echo $STATUS_RESPONSE | jq -r '.status')
  
  if [ "$STATUS" = "COMPLETED" ] || [ "$STATUS" = "FAILED" ]; then
    break
  fi
  
  echo "Evaluation status: $STATUS"
  sleep 2
done

# 5. Get results
curl -s http://localhost:8080/api/v1/evaluations/$EVALUATION_ID/results | jq '.'
```

### JavaScript/Node.js Example

```javascript
const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');

const API_BASE = 'http://localhost:8080/api/v1';

async function uploadAssignment(filePath, title, description) {
  const form = new FormData();
  form.append('file', fs.createReadStream(filePath));
  form.append('title', title);
  form.append('description', description);

  try {
    const response = await axios.post(`${API_BASE}/assignments`, form, {
      headers: form.getHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error uploading assignment:', error.response.data);
    throw error;
  }
}

async function triggerEvaluation(filePath, studentId, assignmentId) {
  const form = new FormData();
  form.append('file', fs.createReadStream(filePath));
  form.append('studentId', studentId);
  form.append('assignmentId', assignmentId);

  try {
    const response = await axios.post(`${API_BASE}/evaluations`, form, {
      headers: form.getHeaders()
    });
    return response.data;
  } catch (error) {
    console.error('Error triggering evaluation:', error.response.data);
    throw error;
  }
}

async function getEvaluationResults(evaluationId) {
  try {
    const response = await axios.get(`${API_BASE}/evaluations/${evaluationId}/results`);
    return response.data;
  } catch (error) {
    console.error('Error getting results:', error.response.data);
    throw error;
  }
}

// Usage example
async function main() {
  try {
    // Upload assignment
    const assignment = await uploadAssignment(
      './Calculator.java',
      'Calculator Assignment',
      'Basic calculator operations'
    );
    console.log('Assignment created:', assignment.assignmentId);

    // Trigger evaluation
    const evaluation = await triggerEvaluation(
      './StudentCalculator.java',
      'student123',
      assignment.assignmentId
    );
    console.log('Evaluation started:', evaluation.evaluationId);

    // Wait and get results
    setTimeout(async () => {
      const results = await getEvaluationResults(evaluation.evaluationId);
      console.log('Results:', JSON.stringify(results, null, 2));
    }, 5000);

  } catch (error) {
    console.error('Error:', error.message);
  }
}

main();
```

## Support

For API support and questions:
- **Documentation**: [API Reference](../api/openapi.yaml)
- **Issues**: Report issues on the project repository
- **Email**: support@studentevaluator.com

## Changelog

### Version 1.0.0
- Initial API release
- Assignment upload and management
- Evaluation processing
- Student score tracking
- Error handling and validation