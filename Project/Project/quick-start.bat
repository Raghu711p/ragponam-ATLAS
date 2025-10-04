@echo off
echo Starting Student Evaluator System...
echo.

echo [1/3] Checking DynamoDB Local...
curl -s http://localhost:8000 >nul 2>&1
if errorlevel 1 (
    echo ERROR: DynamoDB Local not running on port 8000
    echo Please start DynamoDB Local first
    pause
    exit /b 1
) else (
    echo ✓ DynamoDB Local is running
)

echo.
echo [2/3] Building application...
call mvn clean compile -q
if errorlevel 1 (
    echo ERROR: Build failed
    pause
    exit /b 1
) else (
    echo ✓ Build successful
)

echo.
echo [3/3] Starting application...
echo ✓ Using H2 in-memory database (no MySQL setup needed)
echo ✓ DynamoDB Local on port 8000
echo ✓ Application will start on http://localhost:8080
echo ✓ H2 Console available at http://localhost:8080/h2-console
echo.
echo Starting Spring Boot application...
call mvn spring-boot:run

pause