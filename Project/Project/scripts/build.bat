@echo off
REM Student Evaluator System - Local Build Script for Windows
REM This script builds the application locally without containerization

echo ==========================================
echo Student Evaluator System - Local Build
echo ==========================================

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Maven is not installed or not in PATH
    exit /b 1
)

REM Display Java and Maven versions
echo Java version:
java -version
echo.
echo Maven version:
mvn -version
echo.

REM Clean previous builds
echo Cleaning previous builds...
mvn clean
if %errorlevel% neq 0 (
    echo Error: Maven clean failed
    exit /b 1
)

REM Compile the application
echo Compiling application...
mvn compile
if %errorlevel% neq 0 (
    echo Error: Compilation failed
    exit /b 1
)

REM Run tests
echo Running tests...
mvn test
if %errorlevel% neq 0 (
    echo Error: Tests failed
    exit /b 1
)

REM Package the application
echo Packaging application...
mvn package -DskipTests
if %errorlevel% neq 0 (
    echo Error: Packaging failed
    exit /b 1
)

REM Check if JAR was created successfully
set JAR_FILE=target\student-evaluator-system-1.0.0.jar
if exist "%JAR_FILE%" (
    echo Build successful! JAR file created: %JAR_FILE%
    for %%A in ("%JAR_FILE%") do echo File size: %%~zA bytes
) else (
    echo Error: JAR file not found after build
    exit /b 1
)

echo ==========================================
echo Build completed successfully!
echo ==========================================