Feature: Result Retrieval
  As a developer or administrator
  I want to retrieve evaluation results through API and CLI interfaces
  So that I can access student scores and evaluation history

  Background:
    Given the system is running
    And the database is clean
    And I have completed evaluations for multiple students

  Scenario: Retrieve student score via REST API
    Given student "alice-cooper" has a score of 85.5 for assignment "calc-001"
    When I request the score for student "alice-cooper" via REST API
    Then I should receive HTTP status 200
    And the response should contain score 85.5
    And the response should include student ID "alice-cooper"
    And the response should include assignment details

  Scenario: Retrieve student evaluation history via REST API
    Given student "charlie-brown" has multiple evaluation records
    When I request the evaluation history for student "charlie-brown" via REST API
    Then I should receive HTTP status 200
    And the response should contain a list of evaluations
    And each evaluation should include timestamp, assignment, and score
    And the evaluations should be ordered by date descending

  Scenario: Retrieve non-existent student score
    When I request the score for non-existent student "ghost-student" via REST API
    Then I should receive HTTP status 404
    And the error message should indicate "Student not found"

  Scenario: Retrieve student score via CLI
    Given student "david-jones" has a score of 92.0 for assignment "calc-001"
    When I run CLI command "results --student david-jones"
    Then the CLI should display the score 92.0
    And the CLI should show assignment details
    And the CLI should show evaluation timestamp

  Scenario: Retrieve evaluation results by assignment via CLI
    Given assignment "calc-001" has multiple student evaluations
    When I run CLI command "results --assignment calc-001"
    Then the CLI should display all student scores for the assignment
    And the results should be formatted in a readable table
    And the results should include student names and scores

  Scenario: Retrieve detailed evaluation logs
    Given student "eva-green" has an evaluation with detailed logs
    When I request detailed evaluation logs for student "eva-green"
    Then I should receive compilation output
    And I should receive test execution details
    And I should receive any error messages
    And the logs should include timestamps

  Scenario: Cache performance for frequent score lookups
    Given the score cache is populated with student scores
    When I make multiple rapid requests for student scores
    Then the responses should be fast (under 100ms)
    And the cache hit rate should be high
    And the database should not be queried for cached scores

  Scenario: Retrieve evaluation status during processing
    Given an evaluation is currently in progress for student "processing-student"
    When I request the evaluation status via REST API
    Then I should receive HTTP status 200
    And the status should be "IN_PROGRESS"
    And the response should include estimated completion time