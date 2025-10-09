Feature: Assignment Submission
  As a system administrator
  I want to upload Java assignment files and their corresponding JUnit test files
  So that I can set up automated evaluation for student submissions

  Background:
    Given the system is running
    And the database is clean

  Scenario: Successfully upload a Java assignment file
    Given I have a valid Java assignment file "Calculator.java"
    When I upload the assignment file with title "Basic Calculator"
    Then the assignment should be stored successfully
    And the assignment should have an ID
    And the assignment status should be "READY_FOR_TESTS"

  Scenario: Upload JUnit test file for an assignment
    Given I have an assignment with ID "calc-001"
    And I have a valid JUnit test file "CalculatorTest.java"
    When I upload the test file for assignment "calc-001"
    Then the test file should be associated with the assignment
    And the assignment status should be "READY_FOR_EVALUATION"

  Scenario: Reject invalid Java file upload
    Given I have an invalid file "document.txt"
    When I attempt to upload the file as an assignment
    Then the upload should be rejected
    And I should receive an error message "Only .java files are accepted"

  Scenario: Reject oversized file upload
    Given I have a Java file larger than 1MB
    When I attempt to upload the oversized file
    Then the upload should be rejected
    And I should receive an error message "File size exceeds maximum limit"

  Scenario: Upload test file without corresponding assignment
    Given I have a valid JUnit test file "OrphanTest.java"
    When I attempt to upload the test file for non-existent assignment "missing-001"
    Then the upload should be rejected
    And I should receive an error message "Assignment not found"