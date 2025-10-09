-- Test data for evaluation-related repository tests
-- This script inserts sample evaluation data for testing repository methods

-- Insert additional students for testing (if not already present)
INSERT IGNORE INTO students (student_id, name, email, created_at) VALUES
('STU_TEST_001', 'Test Student One', 'test1@example.com', NOW()),
('STU_TEST_002', 'Test Student Two', 'test2@example.com', NOW()),
('STU_TEST_003', 'Test Student Three', 'test3@example.com', NOW());

-- Insert additional assignments for testing (if not already present)
INSERT IGNORE INTO assignments (assignment_id, title, description, test_file_path, created_at) VALUES
('ASG_TEST_001', 'Test Assignment One', 'First test assignment', '/tests/test1.java', NOW()),
('ASG_TEST_002', 'Test Assignment Two', 'Second test assignment', '/tests/test2.java', NOW()),
('ASG_TEST_003', 'Test Assignment Three', 'Third test assignment', NULL, NOW());

-- Insert evaluation records for testing
INSERT IGNORE INTO evaluations (evaluation_id, student_id, assignment_id, score, max_score, status, evaluated_at) VALUES
('EVAL_TEST_001', 'STU_TEST_001', 'ASG_TEST_001', 85.50, 100.00, 'COMPLETED', NOW()),
('EVAL_TEST_002', 'STU_TEST_001', 'ASG_TEST_002', 92.00, 100.00, 'COMPLETED', NOW()),
('EVAL_TEST_003', 'STU_TEST_002', 'ASG_TEST_001', 78.25, 100.00, 'COMPLETED', NOW()),
('EVAL_TEST_004', 'STU_TEST_002', 'ASG_TEST_002', 88.75, 100.00, 'FAILED', NOW()),
('EVAL_TEST_005', 'STU_TEST_003', 'ASG_TEST_001', 95.00, 100.00, 'COMPLETED', NOW()),
('EVAL_TEST_006', 'STU_TEST_003', 'ASG_TEST_003', 0.00, 100.00, 'PENDING', NOW());