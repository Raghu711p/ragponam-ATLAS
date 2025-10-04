@echo off
REM Student Evaluator System - Environment Setup Script for Windows

echo ==========================================
echo Student Evaluator System - Environment Setup
echo ==========================================

REM Configuration
set JAVA_MIN_VERSION=17
set MAVEN_MIN_VERSION=3.6.0

REM Check Java installation
echo Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java %JAVA_MIN_VERSION% or later
    exit /b 1
)

REM Get Java version (simplified check)
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION_STRING=%%g
)
set JAVA_VERSION_STRING=%JAVA_VERSION_STRING:"=%
for /f "delims=. tokens=1-3" %%a in ("%JAVA_VERSION_STRING%") do (
    if "%%a"=="1" (
        set JAVA_VERSION=%%b
    ) else (
        set JAVA_VERSION=%%a
    )
)

echo Found Java version: %JAVA_VERSION%
if %JAVA_VERSION% geq %JAVA_MIN_VERSION% (
    echo ✅ Java version is compatible
) else (
    echo ❌ Java version %JAVA_VERSION% is too old. Minimum required: %JAVA_MIN_VERSION%
    echo Please install Java %JAVA_MIN_VERSION% or later
    exit /b 1
)

REM Check Maven installation
echo.
echo Checking Maven installation...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven is not installed or not in PATH
    echo Please install Maven %MAVEN_MIN_VERSION% or later
    exit /b 1
)

for /f "tokens=3" %%g in ('mvn -version 2^>^&1 ^| findstr /i "Apache Maven"') do (
    set MAVEN_VERSION=%%g
)
echo Found Maven version: %MAVEN_VERSION%
echo ✅ Maven is available

REM Check Git installation (optional)
echo.
echo Checking Git installation...
git --version >nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=3" %%g in ('git --version') do (
        set GIT_VERSION=%%g
    )
    echo Found Git version: %GIT_VERSION%
    echo ✅ Git is available
) else (
    echo ⚠️  Git is not installed (optional but recommended)
)

REM Create necessary directories
echo.
echo Creating project directories...
if not exist "logs" mkdir "logs"
if not exist "data" mkdir "data"
if not exist "data\assignments" mkdir "data\assignments"
if not exist "data\submissions" mkdir "data\submissions"
if not exist "data\test-files" mkdir "data\test-files"
if not exist "target" mkdir "target"
if not exist "target\test-reports" mkdir "target\test-reports"

echo ✅ Project directories created

REM Validate Maven project
echo.
echo Validating Maven project...
if exist "pom.xml" (
    mvn validate >nul 2>&1
    if %errorlevel% equ 0 (
        echo ✅ Maven project validation successful
    ) else (
        echo ❌ Maven project validation failed
        exit /b 1
    )
) else (
    echo ❌ pom.xml not found. Make sure you're in the project root directory
    exit /b 1
)

REM Download dependencies
echo.
echo Downloading Maven dependencies...
mvn dependency:resolve >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Dependencies downloaded successfully
) else (
    echo ❌ Failed to download dependencies
    exit /b 1
)

REM Create local configuration template
echo.
echo Creating local configuration template...
(
echo # Local Development Configuration Template
echo # Copy this file to src/main/resources/application-local.yml and customize as needed
echo.
echo server:
echo   port: 8080
echo.
echo spring:
echo   profiles:
echo     active: local
echo.
echo   datasource:
echo     url: jdbc:h2:file:./data/studentevaluator;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
echo     driver-class-name: org.h2.Driver
echo     username: sa
echo     password: 
echo.
echo   h2:
echo     console:
echo       enabled: true
echo       path: /h2-console
echo.
echo   jpa:
echo     hibernate:
echo       ddl-auto: update
echo     show-sql: true
echo.
echo logging:
echo   level:
echo     com.studentevaluator: DEBUG
echo     org.springframework: INFO
echo   file:
echo     name: logs/application.log
echo.
echo # Application specific settings
echo app:
echo   storage:
echo     base-path: ./data
echo     assignments-path: ./data/assignments
echo     submissions-path: ./data/submissions
echo     test-files-path: ./data/test-files
echo.
echo   evaluation:
echo     timeout-seconds: 30
echo     max-concurrent-evaluations: 5
echo.
echo   security:
echo     max-file-size: 1048576  # 1MB
echo     allowed-extensions: [".java"]
) > application-local.yml.template

echo ✅ Configuration template created: application-local.yml.template

REM Create README for local development
echo.
echo Creating local development README...
(
echo # Local Development Setup
echo.
echo This document describes how to set up and run the Student Evaluator System locally.
echo.
echo ## Prerequisites
echo.
echo - Java %JAVA_MIN_VERSION% or later
echo - Maven %MAVEN_MIN_VERSION% or later
echo - Git (recommended^)
echo.
echo ## Quick Start
echo.
echo 1. **Setup Environment**
echo    ```cmd
echo    scripts\setup-environment.bat
echo    ```
echo.
echo 2. **Build Application**
echo    ```cmd
echo    scripts\build.bat
echo    ```
echo.
echo 3. **Run Tests**
echo    ```cmd
echo    scripts\run-tests.bat
echo    ```
echo.
echo 4. **Deploy Locally**
echo    ```cmd
echo    scripts\deploy-local.bat
echo    ```
echo.
echo 5. **Start Application**
echo    ```cmd
echo    %%USERPROFILE%%\student-evaluator-deploy\start.bat
echo    ```
echo.
echo ## Application URLs
echo.
echo - **Main Application**: http://localhost:8080
echo - **H2 Database Console**: http://localhost:8080/h2-console
echo - **Health Check**: http://localhost:8080/actuator/health
echo.
echo ## Directory Structure
echo.
echo ```
echo project-root/
echo ├── scripts/           # Build and deployment scripts
echo ├── data/             # Local data storage
echo │   ├── assignments/  # Assignment files
echo │   ├── submissions/  # Student submissions
echo │   └── test-files/   # JUnit test files
echo ├── logs/             # Application logs
echo └── target/           # Build artifacts
echo ```
echo.
echo ## Development Workflow
echo.
echo 1. Make code changes
echo 2. Run tests: `scripts\run-tests.bat --type unit`
echo 3. Build application: `scripts\build.bat`
echo 4. Deploy locally: `scripts\deploy-local.bat`
echo 5. Test manually or run integration tests
echo.
echo ## Troubleshooting
echo.
echo - Check logs in `logs\application.log`
echo - Verify Java and Maven versions
echo - Ensure all dependencies are downloaded
echo - Check database connection in H2 console
echo.
echo ## Configuration
echo.
echo Local configuration is stored in:
echo - Development: `application-local.yml.template`
echo - Deployment: `%%USERPROFILE%%\student-evaluator-deploy\config\application-local.yml`
) > LOCAL_DEVELOPMENT.md

echo ✅ Local development README created: LOCAL_DEVELOPMENT.md

echo.
echo ==========================================
echo Environment Setup Complete!
echo ==========================================
echo ✅ Java %JAVA_VERSION% detected
echo ✅ Maven %MAVEN_VERSION% detected
echo ✅ Project directories created
echo ✅ Dependencies downloaded
echo ✅ Configuration templates created
echo.
echo Next steps:
echo 1. Build the application: scripts\build.bat
echo 2. Run tests: scripts\run-tests.bat
echo 3. Deploy locally: scripts\deploy-local.bat
echo.
echo For detailed instructions, see: LOCAL_DEVELOPMENT.md