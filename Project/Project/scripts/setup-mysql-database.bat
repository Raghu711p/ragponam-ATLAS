@echo off
REM Setup MySQL database for Student Evaluator System

set DB_NAME=student_evaluator
set DB_USER=evaluator
set DB_PASSWORD=password
set MYSQL_ROOT_PASSWORD=root

echo Setting up MySQL database for Student Evaluator System...
echo Database: %DB_NAME%
echo User: %DB_USER%

REM Check if MySQL is running
mysql --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: MySQL is not installed or not in PATH
    echo Please install MySQL Server and add it to your PATH
    echo Download from: https://dev.mysql.com/downloads/mysql/
    pause
    exit /b 1
)

echo.
echo Creating database and user...
echo Please enter MySQL root password when prompted.

mysql -u root -p%MYSQL_ROOT_PASSWORD% -e "CREATE DATABASE IF NOT EXISTS %DB_NAME%;"
mysql -u root -p%MYSQL_ROOT_PASSWORD% -e "CREATE USER IF NOT EXISTS '%DB_USER%'@'localhost' IDENTIFIED BY '%DB_PASSWORD%';"
mysql -u root -p%MYSQL_ROOT_PASSWORD% -e "GRANT ALL PRIVILEGES ON %DB_NAME%.* TO '%DB_USER%'@'localhost';"
mysql -u root -p%MYSQL_ROOT_PASSWORD% -e "FLUSH PRIVILEGES;"

if errorlevel 1 (
    echo ERROR: Failed to create database or user
    pause
    exit /b 1
)

echo.
echo Creating database schema...
mysql -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% < database-schema.sql

if errorlevel 1 (
    echo ERROR: Failed to create schema. Creating schema file...
    goto :create_schema
) else (
    echo SUCCESS: Database schema created!
    goto :insert_data
)

:create_schema
echo Creating database schema file...
(
echo -- Student Evaluator System Database Schema
echo.
echo USE %DB_NAME%;
echo.
echo -- Students table
echo CREATE TABLE students ^(
echo     student_id VARCHAR^(50^) PRIMARY KEY,
echo     name VARCHAR^(255^) NOT NULL,
echo     email VARCHAR^(255^) UNIQUE NOT NULL,
echo     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
echo     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
echo ^);
echo.
echo -- Assignments table
echo CREATE TABLE assignments ^(
echo     assignment_id VARCHAR^(50^) PRIMARY KEY,
echo     title VARCHAR^(255^) NOT NULL,
echo     description TEXT,
echo     test_file_path VARCHAR^(500^),
echo     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
echo     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
echo ^);
echo.
echo -- Evaluations table
echo CREATE TABLE evaluations ^(
echo     evaluation_id VARCHAR^(50^) PRIMARY KEY,
echo     student_id VARCHAR^(50^) NOT NULL,
echo     assignment_id VARCHAR^(50^) NOT NULL,
echo     score DECIMAL^(5,2^) DEFAULT 0.00,
echo     max_score DECIMAL^(5,2^) DEFAULT 100.00,
echo     status ENUM^('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED'^) DEFAULT 'PENDING',
echo     submission_file_path VARCHAR^(500^),
echo     compilation_output TEXT,
echo     test_results TEXT,
echo     evaluated_at TIMESTAMP NULL,
echo     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
echo     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
echo     FOREIGN KEY ^(student_id^) REFERENCES students^(student_id^) ON DELETE CASCADE,
echo     FOREIGN KEY ^(assignment_id^) REFERENCES assignments^(assignment_id^) ON DELETE CASCADE,
echo     INDEX idx_student_assignment ^(student_id, assignment_id^),
echo     INDEX idx_status ^(status^),
echo     INDEX idx_evaluated_at ^(evaluated_at^)
echo ^);
echo.
echo -- Create indexes for better performance
echo CREATE INDEX idx_students_email ON students^(email^);
echo CREATE INDEX idx_assignments_title ON assignments^(title^);
echo CREATE INDEX idx_evaluations_score ON evaluations^(score^);
) > database-schema.sql

echo Schema file created. Running schema creation...
mysql -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% < database-schema.sql

:insert_data
echo.
echo Inserting sample data...
(
echo -- Sample data for Student Evaluator System
echo.
echo USE %DB_NAME%;
echo.
echo -- Insert sample students
echo INSERT INTO students ^(student_id, name, email^) VALUES
echo ^('STU001', 'John Doe', 'john.doe@example.com'^),
echo ^('STU002', 'Jane Smith', 'jane.smith@example.com'^),
echo ^('STU003', 'Bob Johnson', 'bob.johnson@example.com'^);
echo.
echo -- Insert sample assignments
echo INSERT INTO assignments ^(assignment_id, title, description, test_file_path^) VALUES
echo ^('ASG001', 'Calculator Implementation', 'Implement a basic calculator with add, subtract, multiply, divide operations', '/tests/CalculatorTest.java'^),
echo ^('ASG002', 'String Utilities', 'Create utility methods for string manipulation', '/tests/StringUtilsTest.java'^),
echo ^('ASG003', 'Data Structures', 'Implement basic data structures: Stack, Queue, LinkedList', '/tests/DataStructuresTest.java'^);
echo.
echo -- Insert sample evaluations
echo INSERT INTO evaluations ^(evaluation_id, student_id, assignment_id, score, max_score, status, evaluated_at^) VALUES
echo ^('EVAL001', 'STU001', 'ASG001', 85.50, 100.00, 'COMPLETED', NOW()^),
echo ^('EVAL002', 'STU001', 'ASG002', 92.00, 100.00, 'COMPLETED', NOW()^),
echo ^('EVAL003', 'STU002', 'ASG001', 78.25, 100.00, 'COMPLETED', NOW()^),
echo ^('EVAL004', 'STU003', 'ASG001', 95.00, 100.00, 'COMPLETED', NOW()^);
) > sample-data.sql

mysql -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% < sample-data.sql

echo.
echo Verifying database setup...
mysql -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% -e "SHOW TABLES;"
mysql -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% -e "SELECT COUNT(*) as student_count FROM students;"
mysql -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% -e "SELECT COUNT(*) as assignment_count FROM assignments;"
mysql -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% -e "SELECT COUNT(*) as evaluation_count FROM evaluations;"

echo.
echo MySQL database setup complete!
echo Database: %DB_NAME%
echo User: %DB_USER%
echo Password: %DB_PASSWORD%
echo.
echo You can now start the Student Evaluator application.

pause