@echo off
REM Start DynamoDB Local for Windows
REM Make sure to update the path to your DynamoDB Local installation

set DYNAMODB_PATH=C:\dynamodb-local
set DYNAMODB_PORT=8000

echo Starting DynamoDB Local...
echo Path: %DYNAMODB_PATH%
echo Port: %DYNAMODB_PORT%

if not exist "%DYNAMODB_PATH%\DynamoDBLocal.jar" (
    echo ERROR: DynamoDB Local not found at %DYNAMODB_PATH%
    echo Please download DynamoDB Local from:
    echo https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.DownloadingAndRunning.html
    echo Extract it to %DYNAMODB_PATH%
    pause
    exit /b 1
)

cd /d "%DYNAMODB_PATH%"
echo Starting DynamoDB Local on port %DYNAMODB_PORT%...
java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb -port %DYNAMODB_PORT%

pause