-- Sample data for Student Evaluator System

USE student_evaluator;

-- Insert sample students
INSERT INTO students (student_id, name, email) VALUES
('STU001', 'John Doe', 'john.doe@example.com'),
('STU002', 'Jane Smith', 'jane.smith@example.com'),
('STU003', 'Bob Johnson', 'bob.johnson@example.com');

-- Insert sample assignments
INSERT INTO assignments (assignment_id, title, description, test_file_path) VALUES
('ASG001', 'Calculator Implementation', 'Implement a basic calculator with add, subtract, multiply, divide operations', '/tests/CalculatorTest.java'),
('ASG002', 'String Utilities', 'Create utility methods for string manipulation', '/tests/StringUtilsTest.java'),
('ASG003', 'Data Structures', 'Implement basic data structures: Stack, Queue, LinkedList', '/tests/DataStructuresTest.java');

-- Insert sample evaluations
INSERT INTO evaluations (evaluation_id, student_id, assignment_id, score, max_score, status, evaluated_at) VALUES
('EVAL001', 'STU001', 'ASG001', 85.50, 100.00, 'COMPLETED', NOW()),
('EVAL002', 'STU001', 'ASG002', 92.00, 100.00, 'COMPLETED', NOW()),
('EVAL003', 'STU002', 'ASG001', 78.25, 100.00, 'COMPLETED', NOW()),
('EVAL004', 'STU003', 'ASG001', 95.00, 100.00, 'COMPLETED', NOW());