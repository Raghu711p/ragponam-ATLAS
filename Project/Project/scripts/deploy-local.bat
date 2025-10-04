@echo off
REM Student Evaluator System - Local Deployment Script for Windows

echo ==========================================
echo Student Evaluator System - Local Deploy
echo ==========================================

REM Configuration
set APP_NAME=student-evaluator-system
set APP_VERSION=1.0.0
set JAR_FILE=target\%APP_NAME%-%APP_VERSION%.jar
set DEPLOY_DIR=%USERPROFILE%\student-evaluator-deploy
set LOG_DIR=%DEPLOY_DIR%\logs
set DATA_DIR=%DEPLOY_DIR%\data
set CONFIG_DIR=%DEPLOY_DIR%\config
set PID_FILE=%DEPLOY_DIR%\%APP_NAME%.pid

REM Create deployment directories
echo Creating deployment directories...
if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%DATA_DIR%" mkdir "%DATA_DIR%"
if not exist "%CONFIG_DIR%" mkdir "%CONFIG_DIR%"

REM Check if JAR file exists
if not exist "%JAR_FILE%" (
    echo Error: JAR file not found: %JAR_FILE%
    echo Please run build script first
    exit /b 1
)

REM Stop existing application if running
if exist "%PID_FILE%" (
    set /p PID=<"%PID_FILE%"
    tasklist /FI "PID eq !PID!" 2>nul | find /I "java.exe" >nul
    if !errorlevel! equ 0 (
        echo Stopping existing application (PID: !PID!)...
        taskkill /PID !PID! /F >nul 2>&1
    )
    del "%PID_FILE%" >nul 2>&1
)

REM Copy JAR file to deployment directory
echo Copying application JAR...
copy "%JAR_FILE%" "%DEPLOY_DIR%\" >nul

REM Create application configuration
echo Creating application configuration...
(
echo server:
echo   port: 8080
echo   servlet:
echo     context-path: /
echo.
echo spring:
echo   application:
echo     name: student-evaluator-system
echo.
echo   datasource:
echo     url: jdbc:h2:file:%DATA_DIR:\=/%/studentevaluator;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
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
echo     show-sql: false
echo     properties:
echo       hibernate:
echo         format_sql: true
echo.
echo logging:
echo   level:
echo     com.studentevaluator: INFO
echo     org.springframework: WARN
echo   file:
echo     name: %LOG_DIR:\=/%/application.log
echo   pattern:
echo     file: "%%d{yyyy-MM-dd HH:mm:ss} [%%thread] %%-5level %%logger{36} - %%msg%%n"
echo     console: "%%d{HH:mm:ss} %%-5level %%logger{36} - %%msg%%n"
echo.
echo # Application specific settings
echo app:
echo   storage:
echo     base-path: %DATA_DIR:\=/%
echo     assignments-path: %DATA_DIR:\=/%/assignments
echo     submissions-path: %DATA_DIR:\=/%/submissions
echo     test-files-path: %DATA_DIR:\=/%/test-files
echo.
echo   evaluation:
echo     timeout-seconds: 30
echo     max-concurrent-evaluations: 5
echo.
echo   security:
echo     max-file-size: 1048576  # 1MB
echo     allowed-extensions: [".java"]
) > "%CONFIG_DIR%\application-local.yml"

REM Create startup script
echo Creating startup script...
(
echo @echo off
echo cd /d "%DEPLOY_DIR%"
echo.
echo REM Set JVM options
echo set JAVA_OPTS=-Xms512m -Xmx1024m -Dspring.profiles.active=local -Dspring.config.location=file:%CONFIG_DIR:\=/%/application-local.yml
echo.
echo REM Start the application
echo echo Starting Student Evaluator System...
echo start /B java %%JAVA_OPTS%% -jar %APP_NAME%-%APP_VERSION%.jar ^> "%LOG_DIR%\startup.log" 2^>^&1
echo.
echo REM Wait a moment for the process to start
echo timeout /t 2 /nobreak ^>nul
echo.
echo REM Find and save PID
echo for /f "tokens=2" %%%%i in ('tasklist /FI "IMAGENAME eq java.exe" /FO CSV ^| find "java.exe"'^) do (
echo     echo %%%%~i ^> "%PID_FILE%"
echo     goto :found
echo ^)
echo :found
echo.
echo if exist "%PID_FILE%" (
echo     set /p PID^<"%PID_FILE%"
echo     echo Application started with PID: %%PID%%
echo ^) else (
echo     echo Warning: Could not determine PID
echo ^)
echo.
echo echo Logs available at: %LOG_DIR%\
echo echo Application will be available at: http://localhost:8080
echo echo H2 Console available at: http://localhost:8080/h2-console
) > "%DEPLOY_DIR%\start.bat"

REM Create stop script
echo Creating stop script...
(
echo @echo off
echo.
echo if exist "%PID_FILE%" (
echo     set /p PID^<"%PID_FILE%"
echo     tasklist /FI "PID eq %%PID%%" 2^>nul ^| find /I "java.exe" ^>nul
echo     if %%errorlevel%% equ 0 (
echo         echo Stopping Student Evaluator System (PID: %%PID%%^)...
echo         taskkill /PID %%PID%% /F ^>nul 2^>^&1
echo         echo Application stopped
echo     ^) else (
echo         echo Application is not running
echo     ^)
echo     del "%PID_FILE%" ^>nul 2^>^&1
echo ^) else (
echo     echo PID file not found. Application may not be running.
echo ^)
) > "%DEPLOY_DIR%\stop.bat"

REM Create status script
echo Creating status script...
(
echo @echo off
echo.
echo if exist "%PID_FILE%" (
echo     set /p PID^<"%PID_FILE%"
echo     tasklist /FI "PID eq %%PID%%" 2^>nul ^| find /I "java.exe" ^>nul
echo     if %%errorlevel%% equ 0 (
echo         echo Student Evaluator System is running (PID: %%PID%%^)
echo         echo Application URL: http://localhost:8080
echo         echo H2 Console URL: http://localhost:8080/h2-console
echo         echo Log file: %LOG_DIR%\application.log
echo     ^) else (
echo         echo Student Evaluator System is not running (stale PID file^)
echo         del "%PID_FILE%" ^>nul 2^>^&1
echo     ^)
echo ^) else (
echo     echo Student Evaluator System is not running
echo ^)
) > "%DEPLOY_DIR%\status.bat"

REM Create data directories
echo Creating data directories...
if not exist "%DATA_DIR%\assignments" mkdir "%DATA_DIR%\assignments"
if not exist "%DATA_DIR%\submissions" mkdir "%DATA_DIR%\submissions"
if not exist "%DATA_DIR%\test-files" mkdir "%DATA_DIR%\test-files"

echo ==========================================
echo Deployment completed successfully!
echo ==========================================
echo Deployment directory: %DEPLOY_DIR%
echo Configuration file: %CONFIG_DIR%\application-local.yml
echo Log directory: %LOG_DIR%
echo Data directory: %DATA_DIR%
echo.
echo To start the application:
echo   %DEPLOY_DIR%\start.bat
echo.
echo To stop the application:
echo   %DEPLOY_DIR%\stop.bat
echo.
echo To check status:
echo   %DEPLOY_DIR%\status.bat