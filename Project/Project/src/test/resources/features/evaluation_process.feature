Feature: Evaluation Process
  As a system administrator
  I want the system to automatically compile and test student submissions
  So that I can get consistent and objective evaluation results

  Background:
    Given the system is running
    And the database is clean
    And I have an assignment "calc-001" with test files ready

  Scenario: Successfully evaluate a correct student submission
    Given I have a student "john-doe" registered in the system
    And I have a correct Java submission "Calculator.java" for assignment "calc-001"
    When I trigger evaluation for student "john-doe" and assignment "calc-001"
    Then the compilation should succeed
    And all tests should pass
    And the evaluation result should have score 100.0
    And the evaluation status should be "COMPLETED"
    And the result should be stored in MySQL
    And the execution logs should be stored in DynamoDB

  Scenario: Evaluate submission with compilation errors
    Given I have a student "jane-smith" registered in the system
    And I have a Java submission with syntax errors for assignment "calc-001"
    When I trigger evaluation for student "jane-smith" and assignment "calc-001"
    Then the compilation should fail
    And the evaluation status should be "FAILED"
    And the error message should contain compilation details
    And the result should be stored in MySQL with score 0.0
    And the compilation errors should be logged in DynamoDB

  Scenario: Evaluate submission with failing tests
    Given I have a student "bob-wilson" registered in the system
    And I have a Java submission with logical errors for assignment "calc-001"
    When I trigger evaluation for student "bob-wilson" and assignment "calc-001"
    Then the compilation should succeed
    And some tests should fail
    And the evaluation result should have partial score
    And the evaluation status should be "COMPLETED"
    And the test results should be stored in MySQL
    And the test execution logs should be stored in DynamoDB

  Scenario: Handle timeout during test execution
    Given I have a student "slow-student" registered in the system
    And I have a Java submission with infinite loop for assignment "calc-001"
    When I trigger evaluation for student "slow-student" and assignment "calc-001"
    Then the compilation should succeed
    And the test execution should timeout
    And the evaluation status should be "FAILED"
    And the timeout error should be logged

  Scenario: Evaluate multiple submissions concurrently
    Given I have students "student1", "student2", "student3" registered in the system
    And I have valid Java submissions for all students for assignment "calc-001"
    When I trigger evaluation for all students simultaneously
    Then all evaluations should complete successfully
    And each student should have their individual results
    And the score cache should be updated for all students