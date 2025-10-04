package com.studentevaluator.repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.Select;

/**
 * Repository class for DynamoDB operations.
 * Handles storage and retrieval of unstructured evaluation logs.
 */
@Repository
public class DynamoDBRepository {
    
    private static final String TABLE_NAME = "evaluation_logs";
    private static final String EVALUATION_ID_ATTR = "evaluation_id";
    private static final String TIMESTAMP_ATTR = "timestamp";
    private static final String LOG_TYPE_ATTR = "log_type";
    private static final String LOG_DATA_ATTR = "log_data";
    private static final String STUDENT_ID_ATTR = "student_id";
    private static final String ASSIGNMENT_ID_ATTR = "assignment_id";
    
    private final DynamoDbClient dynamoDbClient;
    private final DateTimeFormatter timestampFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    @Autowired
    public DynamoDBRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }
    
    /**
     * Store evaluation log entry in DynamoDB.
     * 
     * @param evaluationId the evaluation ID
     * @param logType the type of log (COMPILATION, TEST_EXECUTION, ERROR, etc.)
     * @param logData the actual log data/content
     * @param studentId the student ID (optional)
     * @param assignmentId the assignment ID (optional)
     */
    public void storeEvaluationLog(String evaluationId, String logType, String logData, 
                                  String studentId, String assignmentId) {
        String timestamp = LocalDateTime.now().format(timestampFormatter);
        
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(EVALUATION_ID_ATTR, AttributeValue.builder().s(evaluationId).build());
        item.put(TIMESTAMP_ATTR, AttributeValue.builder().s(timestamp).build());
        item.put(LOG_TYPE_ATTR, AttributeValue.builder().s(logType).build());
        item.put(LOG_DATA_ATTR, AttributeValue.builder().s(logData).build());
        
        if (studentId != null && !studentId.trim().isEmpty()) {
            item.put(STUDENT_ID_ATTR, AttributeValue.builder().s(studentId).build());
        }
        
        if (assignmentId != null && !assignmentId.trim().isEmpty()) {
            item.put(ASSIGNMENT_ID_ATTR, AttributeValue.builder().s(assignmentId).build());
        }
        
        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
        
        dynamoDbClient.putItem(request);
    }
    
    /**
     * Store compilation log.
     * 
     * @param evaluationId the evaluation ID
     * @param compilationOutput the compilation output/errors
     * @param studentId the student ID
     * @param assignmentId the assignment ID
     */
    public void storeCompilationLog(String evaluationId, String compilationOutput, 
                                   String studentId, String assignmentId) {
        storeEvaluationLog(evaluationId, "COMPILATION", compilationOutput, studentId, assignmentId);
    }
    
    /**
     * Store test execution log.
     * 
     * @param evaluationId the evaluation ID
     * @param testOutput the test execution output
     * @param studentId the student ID
     * @param assignmentId the assignment ID
     */
    public void storeTestExecutionLog(String evaluationId, String testOutput, 
                                     String studentId, String assignmentId) {
        storeEvaluationLog(evaluationId, "TEST_EXECUTION", testOutput, studentId, assignmentId);
    }
    
    /**
     * Store error log.
     * 
     * @param evaluationId the evaluation ID
     * @param errorMessage the error message/stack trace
     * @param studentId the student ID
     * @param assignmentId the assignment ID
     */
    public void storeErrorLog(String evaluationId, String errorMessage, 
                             String studentId, String assignmentId) {
        storeEvaluationLog(evaluationId, "ERROR", errorMessage, studentId, assignmentId);
    }
    
    /**
     * Retrieve all logs for a specific evaluation.
     * 
     * @param evaluationId the evaluation ID
     * @return list of log entries for the evaluation
     */
    public List<Map<String, AttributeValue>> getEvaluationLogs(String evaluationId) {
        QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .keyConditionExpression(EVALUATION_ID_ATTR + " = :evaluationId")
                .expressionAttributeValues(Map.of(
                        ":evaluationId", AttributeValue.builder().s(evaluationId).build()
                ))
                .scanIndexForward(true) // Sort by timestamp ascending
                .build();
        
        QueryResponse response = dynamoDbClient.query(request);
        return response.items();
    }
    
    /**
     * Retrieve logs for a specific evaluation and log type.
     * 
     * @param evaluationId the evaluation ID
     * @param logType the log type to filter by
     * @return list of log entries matching the criteria
     */
    public List<Map<String, AttributeValue>> getEvaluationLogsByType(String evaluationId, String logType) {
        QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .keyConditionExpression(EVALUATION_ID_ATTR + " = :evaluationId")
                .filterExpression(LOG_TYPE_ATTR + " = :logType")
                .expressionAttributeValues(Map.of(
                        ":evaluationId", AttributeValue.builder().s(evaluationId).build(),
                        ":logType", AttributeValue.builder().s(logType).build()
                ))
                .scanIndexForward(true)
                .build();
        
        QueryResponse response = dynamoDbClient.query(request);
        return response.items();
    }
    
    /**
     * Retrieve logs within a time range for an evaluation.
     * 
     * @param evaluationId the evaluation ID
     * @param startTime the start time
     * @param endTime the end time
     * @return list of log entries within the time range
     */
    public List<Map<String, AttributeValue>> getEvaluationLogsInTimeRange(String evaluationId, 
                                                                          LocalDateTime startTime, 
                                                                          LocalDateTime endTime) {
        String startTimestamp = startTime.format(timestampFormatter);
        String endTimestamp = endTime.format(timestampFormatter);
        
        QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .keyConditionExpression(EVALUATION_ID_ATTR + " = :evaluationId AND " +
                                      TIMESTAMP_ATTR + " BETWEEN :startTime AND :endTime")
                .expressionAttributeValues(Map.of(
                        ":evaluationId", AttributeValue.builder().s(evaluationId).build(),
                        ":startTime", AttributeValue.builder().s(startTimestamp).build(),
                        ":endTime", AttributeValue.builder().s(endTimestamp).build()
                ))
                .scanIndexForward(true)
                .build();
        
        QueryResponse response = dynamoDbClient.query(request);
        return response.items();
    }
    
    /**
     * Delete all logs for a specific evaluation.
     * 
     * @param evaluationId the evaluation ID
     * @return number of deleted items
     */
    public int deleteEvaluationLogs(String evaluationId) {
        // First, get all items for the evaluation
        List<Map<String, AttributeValue>> items = getEvaluationLogs(evaluationId);
        
        int deletedCount = 0;
        for (Map<String, AttributeValue> item : items) {
            Map<String, AttributeValue> key = Map.of(
                    EVALUATION_ID_ATTR, item.get(EVALUATION_ID_ATTR),
                    TIMESTAMP_ATTR, item.get(TIMESTAMP_ATTR)
            );
            
            DeleteItemRequest deleteRequest = DeleteItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(key)
                    .build();
            
            dynamoDbClient.deleteItem(deleteRequest);
            deletedCount++;
        }
        
        return deletedCount;
    }
    
    /**
     * Get logs for multiple evaluations (batch operation).
     * 
     * @param evaluationIds list of evaluation IDs
     * @return map of evaluation ID to list of log entries
     */
    public Map<String, List<Map<String, AttributeValue>>> getBatchEvaluationLogs(List<String> evaluationIds) {
        Map<String, List<Map<String, AttributeValue>>> result = new HashMap<>();
        
        // DynamoDB batch operations have limitations, so we'll query each evaluation separately
        for (String evaluationId : evaluationIds) {
            List<Map<String, AttributeValue>> logs = getEvaluationLogs(evaluationId);
            result.put(evaluationId, logs);
        }
        
        return result;
    }
    
    /**
     * Count logs for a specific evaluation.
     * 
     * @param evaluationId the evaluation ID
     * @return count of log entries
     */
    public int countEvaluationLogs(String evaluationId) {
        QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .keyConditionExpression(EVALUATION_ID_ATTR + " = :evaluationId")
                .expressionAttributeValues(Map.of(
                        ":evaluationId", AttributeValue.builder().s(evaluationId).build()
                ))
                .select(Select.COUNT)
                .build();
        
        QueryResponse response = dynamoDbClient.query(request);
        return response.count();
    }
    
    /**
     * Save error log entry to DynamoDB for troubleshooting and analysis.
     * 
     * @param errorLog the error log data as a map
     */
    public void saveErrorLog(Map<String, Object> errorLog) {
        Map<String, AttributeValue> item = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : errorLog.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value != null) {
                if (value instanceof String) {
                    item.put(key, AttributeValue.builder().s((String) value).build());
                } else if (value instanceof Number) {
                    item.put(key, AttributeValue.builder().n(value.toString()).build());
                } else if (value instanceof Boolean) {
                    item.put(key, AttributeValue.builder().bool((Boolean) value).build());
                } else {
                    // Convert complex objects to JSON string
                    item.put(key, AttributeValue.builder().s(value.toString()).build());
                }
            }
        }
        
        // Use error_logs table for error logging
        PutItemRequest request = PutItemRequest.builder()
                .tableName("error_logs")
                .item(item)
                .build();
        
        try {
            dynamoDbClient.putItem(request);
        } catch (Exception e) {
            // Don't let error logging failures break the application
            System.err.println("Failed to save error log to DynamoDB: " + e.getMessage());
        }
    }
    
    /**
     * Check if table exists and create if it doesn't.
     * This method should be called during application startup.
     */
    public void ensureTableExists() {
        try {
            DescribeTableRequest describeRequest = DescribeTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .build();
            
            dynamoDbClient.describeTable(describeRequest);
            // Table exists
        } catch (ResourceNotFoundException e) {
            // Table doesn't exist, create it
            createTable();
        }
        
        // Also ensure error_logs table exists
        try {
            DescribeTableRequest errorLogsDescribeRequest = DescribeTableRequest.builder()
                    .tableName("error_logs")
                    .build();
            
            dynamoDbClient.describeTable(errorLogsDescribeRequest);
            // Table exists
        } catch (ResourceNotFoundException e) {
            // Table doesn't exist, create it
            createErrorLogsTable();
        }
    }
    
    /**
     * Create the evaluation_logs table.
     */
    private void createTable() {
        CreateTableRequest request = CreateTableRequest.builder()
                .tableName(TABLE_NAME)
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName(EVALUATION_ID_ATTR)
                                .keyType(KeyType.HASH)
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName(TIMESTAMP_ATTR)
                                .keyType(KeyType.RANGE)
                                .build()
                )
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName(EVALUATION_ID_ATTR)
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName(TIMESTAMP_ATTR)
                                .attributeType(ScalarAttributeType.S)
                                .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
        
        dynamoDbClient.createTable(request);
    }
    
    /**
     * Create the error_logs table for error logging.
     */
    private void createErrorLogsTable() {
        CreateTableRequest request = CreateTableRequest.builder()
                .tableName("error_logs")
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName("error_id")
                                .keyType(KeyType.HASH)
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName("timestamp")
                                .keyType(KeyType.RANGE)
                                .build()
                )
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName("error_id")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("timestamp")
                                .attributeType(ScalarAttributeType.S)
                                .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
        
        dynamoDbClient.createTable(request);
    }
}