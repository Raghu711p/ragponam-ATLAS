@echo off
REM Setup DynamoDB tables for local development

set ENDPOINT_URL=http://localhost:8000
set TABLE_NAME=evaluation_logs

echo Setting up DynamoDB tables for local development...
echo Endpoint: %ENDPOINT_URL%

REM Check if DynamoDB Local is running
curl -s %ENDPOINT_URL% >nul 2>&1
if errorlevel 1 (
    echo ERROR: DynamoDB Local is not running on %ENDPOINT_URL%
    echo Please start DynamoDB Local first using start-dynamodb-local.bat
    pause
    exit /b 1
)

echo Creating evaluation_logs table...
aws dynamodb create-table ^
    --endpoint-url %ENDPOINT_URL% ^
    --table-name %TABLE_NAME% ^
    --attribute-definitions ^
        AttributeName=evaluation_id,AttributeType=S ^
        AttributeName=timestamp,AttributeType=S ^
        AttributeName=student_id,AttributeType=S ^
    --key-schema ^
        AttributeName=evaluation_id,KeyType=HASH ^
        AttributeName=timestamp,KeyType=RANGE ^
    --global-secondary-indexes ^
        IndexName=StudentIndex,KeySchema=[{AttributeName=student_id,KeyType=HASH},{AttributeName=timestamp,KeyType=RANGE}],Projection={ProjectionType=ALL} ^
    --billing-mode PAY_PER_REQUEST

if errorlevel 1 (
    echo ERROR: Failed to create table. It might already exist.
) else (
    echo SUCCESS: Table %TABLE_NAME% created successfully!
)

echo.
echo Waiting for table to be active...
aws dynamodb wait table-exists --endpoint-url %ENDPOINT_URL% --table-name %TABLE_NAME%

echo.
echo Inserting sample data...
aws dynamodb put-item ^
    --endpoint-url %ENDPOINT_URL% ^
    --table-name %TABLE_NAME% ^
    --item "{\"evaluation_id\": {\"S\": \"EVAL001\"}, \"timestamp\": {\"S\": \"2024-01-15T10:30:00Z\"}, \"student_id\": {\"S\": \"STU001\"}, \"assignment_id\": {\"S\": \"ASG001\"}, \"action\": {\"S\": \"EVALUATION_STARTED\"}, \"details\": {\"S\": \"Student submitted Calculator.java for evaluation\"}, \"metadata\": {\"M\": {\"file_size\": {\"N\": \"1024\"}, \"submission_time\": {\"S\": \"2024-01-15T10:29:45Z\"}}}}"

aws dynamodb put-item ^
    --endpoint-url %ENDPOINT_URL% ^
    --table-name %TABLE_NAME% ^
    --item "{\"evaluation_id\": {\"S\": \"EVAL002\"}, \"timestamp\": {\"S\": \"2024-01-15T10:35:00Z\"}, \"student_id\": {\"S\": \"STU001\"}, \"assignment_id\": {\"S\": \"ASG001\"}, \"action\": {\"S\": \"EVALUATION_COMPLETED\"}, \"details\": {\"S\": \"Evaluation completed with score 85.5\"}, \"metadata\": {\"M\": {\"score\": {\"N\": \"85.5\"}, \"max_score\": {\"N\": \"100\"}}}}"

echo.
echo Verifying setup...
aws dynamodb list-tables --endpoint-url %ENDPOINT_URL%

echo.
echo DynamoDB setup complete!
echo You can now start the Student Evaluator application.

pause