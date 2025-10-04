Feature: End-to-End Workflow
  As a system user
  I want to perform complete workflows from assignment setup to result retrieval
  So that I can validate the entire system functionality

  Background:
    Given the system is running
    And the database is clean

  Scenario: Complete assignment lifecycle via REST API
    Given I am using the REST API interface
    When I upload assignment "MathUtils.java" with title "Math Utilities"
    And I upload test file "MathUtilsTest.java" for the assignment
    And I register student "workflow-student-1"
    And I submit student code for evaluation
    And I wait for evaluation to complete
    Then I should be able to retrieve the evaluation results
    And the results should include compilation status
    And the results should include test execution results
    And the results should include final score
    And the student score should be cached for quick access

  Scenario: Complete assignment lifecycle via CLI
    Given I am using the CLI interface
    When I run "submit --file MathUtils.java --student workflow-student-2 --assignment math-utils"
    And I run "upload-tests --file MathUtilsTest.java --assignment math-utils"
    And I run "evaluate --student workflow-student-2 --assignment math-utils"
    And I wait for evaluation to complete
    And I run "results --student workflow-student-2"
    Then the CLI should show successful evaluation
    And the CLI should display the final score
    And the CLI should show evaluation timestamp

  Scenario: Mixed API and CLI usage
    Given I have uploaded an assignment via REST API
    And I have uploaded test files via REST API
    When I submit student code via CLI
    And I trigger evaluation via REST API
    Then I should be able to retrieve results via both API and CLI
    And the results should be consistent across interfaces

  Scenario: Batch processing multiple students
    Given I have assignment "BatchTest.java" with test files ready
    And I have 5 students registered in the system
    When I submit code for all 5 students
    And I trigger batch evaluation
    Then all evaluations should complete within reasonable time
    And each student should have individual results
    And the system should handle concurrent processing
    And all results should be stored correctly

  Scenario: Error recovery and system resilience
    Given I have a working assignment setup
    When I submit invalid code that causes compilation errors
    And I submit code that causes test timeouts
    And I submit valid code after the errors
    Then the system should handle all scenarios gracefully
    And valid submissions should still process correctly
    And error logs should be properly recorded
    And the system should remain stable