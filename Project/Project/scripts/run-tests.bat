@echo off
REM Student Evaluator System - Test Automation Script for Windows

echo ==========================================
echo Student Evaluator System - Test Runner
echo ==========================================

REM Configuration
set REPORTS_DIR=target\test-reports
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set TIMESTAMP=%dt:~0,8%_%dt:~8,6%

REM Create reports directory
if not exist "%REPORTS_DIR%" mkdir "%REPORTS_DIR%"

REM Default values
set TEST_TYPE=all
set SPECIFIC_TEST=

REM Parse command line arguments
:parse_args
if "%~1"=="" goto :run_tests
if "%~1"=="--type" (
    set TEST_TYPE=%~2
    shift
    shift
    goto :parse_args
)
if "%~1"=="--test" (
    set SPECIFIC_TEST=%~2
    shift
    shift
    goto :parse_args
)
if "%~1"=="--help" (
    echo Usage: %0 [OPTIONS]
    echo.
    echo Options:
    echo   --type TYPE     Test type to run (unit^|integration^|bdd^|all^)
    echo   --test PATTERN  Specific test pattern to run
    echo   --help          Show this help message
    echo.
    echo Examples:
    echo   %0                           # Run all tests
    echo   %0 --type unit              # Run only unit tests
    echo   %0 --type integration       # Run only integration tests
    echo   %0 --type bdd               # Run only BDD tests
    echo   %0 --test "*Service*"       # Run tests matching pattern
    exit /b 0
)
echo Unknown option: %~1
echo Use --help for usage information
exit /b 1

:run_tests
REM Clean previous test results
echo Cleaning previous test results...
mvn clean
if %errorlevel% neq 0 (
    echo Error: Maven clean failed
    exit /b 1
)

REM Run tests based on type
if "%TEST_TYPE%"=="unit" (
    call :run_unit_tests
) else if "%TEST_TYPE%"=="integration" (
    call :run_integration_tests
) else if "%TEST_TYPE%"=="bdd" (
    call :run_bdd_tests
) else if "%TEST_TYPE%"=="all" (
    call :run_all_tests
) else if not "%SPECIFIC_TEST%"=="" (
    call :run_specific_tests
) else (
    echo Invalid test type: %TEST_TYPE%
    echo Valid types: unit, integration, bdd, all
    exit /b 1
)

REM Generate test summary
call :generate_summary

echo.
echo Test execution completed at: %date% %time%
exit /b 0

:run_unit_tests
echo.
echo Running Unit Tests...
echo ----------------------------------------
mvn test -Dtest="!*IntegrationTest,!*IT,!*BDD*" -Dmaven.test.failure.ignore=true
call :copy_reports "unit"
goto :eof

:run_integration_tests
echo.
echo Running Integration Tests...
echo ----------------------------------------
mvn test -Dtest="*IntegrationTest,*IT" -Dmaven.test.failure.ignore=true
call :copy_reports "integration"
goto :eof

:run_bdd_tests
echo.
echo Running BDD Tests...
echo ----------------------------------------
mvn test -Dtest="*CucumberTestRunner" -Dmaven.test.failure.ignore=true
call :copy_reports "bdd"
goto :eof

:run_all_tests
call :run_unit_tests
call :run_integration_tests
call :run_bdd_tests
goto :eof

:run_specific_tests
echo.
echo Running Custom Test Pattern: %SPECIFIC_TEST%...
echo ----------------------------------------
mvn test -Dtest="%SPECIFIC_TEST%" -Dmaven.test.failure.ignore=true
call :copy_reports "custom"
goto :eof

:copy_reports
if exist "target\surefire-reports" (
    if not exist "%REPORTS_DIR%\%~1_%TIMESTAMP%" mkdir "%REPORTS_DIR%\%~1_%TIMESTAMP%"
    xcopy "target\surefire-reports\*" "%REPORTS_DIR%\%~1_%TIMESTAMP%\" /E /I /Q >nul
)
goto :eof

:generate_summary
echo.
echo ==========================================
echo Test Execution Summary
echo ==========================================

if exist "target\surefire-reports" (
    REM Count test results (simplified for Windows batch)
    set TOTAL_TESTS=0
    set FAILED_TESTS=0
    
    for /f %%i in ('dir /b target\surefire-reports\TEST-*.xml 2^>nul ^| find /c /v ""') do set TOTAL_TESTS=%%i
    
    if %TOTAL_TESTS% gtr 0 (
        echo Total Test Files: %TOTAL_TESTS%
        echo.
        echo For detailed results, check:
        echo   HTML Report: target\surefire-reports\
        echo   Archived Reports: %REPORTS_DIR%\
        
        REM BDD specific reports
        if "%TEST_TYPE%"=="bdd" (
            if exist "target\cucumber-reports" (
                echo   BDD Reports: target\cucumber-reports\
            )
        )
        if "%TEST_TYPE%"=="all" (
            if exist "target\cucumber-reports" (
                echo   BDD Reports: target\cucumber-reports\
            )
        )
    ) else (
        echo No test results found.
    )
) else (
    echo No test results found.
)
goto :eof