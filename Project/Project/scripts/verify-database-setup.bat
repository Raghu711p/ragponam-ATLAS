@echo off
REM Verify database setup for Student Evaluator System

echo Verifying Student Evaluator Database Setup...
echo =============================================

REM Test MySQL Connection
echo.
echo [1/4] Testing MySQL Connection...
mysql -u evaluator -ppassword student_evaluator -e "SELECT 'MySQL Connection: SUCCESS' as status;" 2>nul
if errorlevel 1 (
    echo MySQL Connection: FAILED
    echo - Check if MySQL server is running
    echo - Verify database 'student_evaluator' exists
    echo - Verify user 'evaluator' with password 'password' exists
    set MYSQL_OK=0
) else (
    echo MySQL Connection: SUCCESS
    set MYSQL_OK=1
)

REM Test MySQL Tables
echo.
echo [2/4] Testing MySQL Tables...
if %MYSQL_OK%==1 (
    mysql -u evaluator -ppassword student_evaluator -e "SELECT COUNT(*) as students FROM students; SELECT COUNT(*) as assignments FROM assignments; SELECT COUNT(*) as evaluations FROM evaluations;" 2>nul
    if errorlevel 1 (
        echo MySQL Tables: FAILED - Tables not found or empty
    ) else (
        echo MySQL Tables: SUCCESS
    )
) else (
    echo MySQL Tables: SKIPPED - MySQL connection failed
)

REM Test DynamoDB Local Connection
echo.
echo [3/4] Testing DynamoDB Local Connection...
curl -s http://localhost:8000 >nul 2>&1
if errorlevel 1 (
    echo DynamoDB Local Connection: FAILED
    echo - Check if DynamoDB Local is running on port 8000
    echo - Run 'scripts\start-dynamodb-local.bat' to start it
    set DYNAMO_OK=0
) else (
    echo DynamoDB Local Connection: SUCCESS
    set DYNAMO_OK=1
)

REM Test DynamoDB Tables
echo.
echo [4/4] Testing DynamoDB Tables...
if %DYNAMO_OK%==1 (
    aws dynamodb describe-table --endpoint-url http://localhost:8000 --table-name evaluation_logs >nul 2>&1
    if errorlevel 1 (
        echo DynamoDB Tables: FAILED - Table 'evaluation_logs' not found
        echo - Run 'scripts\setup-dynamodb-tables.bat' to create tables
    ) else (
        echo DynamoDB Tables: SUCCESS
        aws dynamodb scan --endpoint-url http://localhost:8000 --table-name evaluation_logs --select COUNT 2>nul | findstr "Count"
    )
) else (
    echo DynamoDB Tables: SKIPPED - DynamoDB Local connection failed
)

echo.
echo =============================================
echo Database Verification Complete
echo.

REM Summary
if %MYSQL_OK%==1 if %DYNAMO_OK%==1 (
    echo STATUS: ALL SYSTEMS READY
    echo You can now start the Student Evaluator application with:
    echo   mvnw spring-boot:run
) else (
    echo STATUS: SETUP INCOMPLETE
    if %MYSQL_OK%==0 echo - Fix MySQL setup issues
    if %DYNAMO_OK%==0 echo - Fix DynamoDB Local setup issues
    echo.
    echo Quick fix commands:
    echo   scripts\setup-mysql-database.bat
    echo   scripts\start-dynamodb-local.bat
    echo   scripts\setup-dynamodb-tables.bat
)

echo.
pause