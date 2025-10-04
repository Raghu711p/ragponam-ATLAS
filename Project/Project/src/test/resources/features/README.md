# BDD Feature Files

This directory contains Gherkin feature files that define the behavior-driven development (BDD) scenarios for the Student Evaluator System.

## Feature Files

### 1. assignment_submission.feature
Tests the assignment and test file upload functionality:
- Valid Java file uploads
- JUnit test file association
- File validation and error handling
- Size limit enforcement

### 2. evaluation_process.feature
Tests the core evaluation workflow:
- Compilation of student submissions
- JUnit test execution
- Score calculation and storage
- Error handling for compilation and runtime issues
- Concurrent evaluation processing

### 3. result_retrieval.feature
Tests result access through API and CLI:
- Score retrieval via REST API
- Evaluation history access
- CLI result display
- Cache performance validation
- Error handling for non-existent data

### 4. end_to_end_workflow.feature
Tests complete system workflows:
- Full assignment lifecycle via REST API
- Complete workflow via CLI
- Mixed API/CLI usage
- Batch processing scenarios
- System resilience and error recovery

## Running BDD Tests

### Via Maven
```bash
# Run all tests including BDD
mvn test

# Run only BDD tests
mvn test -Dtest=CucumberTestRunner

# Run integration tests (includes BDD)
mvn verify
```

### Via IDE
Run the `CucumberTestRunner` class directly from your IDE.

### Generating Reports
BDD test reports are generated in:
- `target/cucumber-reports/` - HTML reports
- `target/cucumber-reports/Cucumber.json` - JSON format
- `target/cucumber-reports/Cucumber.xml` - JUnit XML format

## Test Data

Test data files are located in `src/test/resources/bdd-test-data/`:
- `Calculator.java` - Sample assignment file
- `CalculatorTest.java` - Sample JUnit test file

## Configuration

BDD test configuration is managed through:
- `cucumber.properties` - Cucumber-specific settings
- `CucumberSpringConfiguration.java` - Spring Boot integration
- `application-test.yml` - Test-specific application properties

## Step Definitions

Step definitions are organized by feature area:
- `CommonSteps.java` - Shared setup and utility steps
- `AssignmentSubmissionSteps.java` - Assignment upload scenarios
- `EvaluationProcessSteps.java` - Evaluation workflow scenarios
- `ResultRetrievalSteps.java` - Result access scenarios
- `EndToEndWorkflowSteps.java` - Complete workflow scenarios

## Best Practices

1. **Scenario Independence**: Each scenario should be independent and not rely on state from other scenarios
2. **Data Cleanup**: The `@Given("the database is clean")` step ensures clean state for each scenario
3. **Realistic Data**: Use realistic test data that represents actual usage patterns
4. **Error Scenarios**: Include both happy path and error scenarios
5. **Performance Testing**: Include scenarios that validate performance requirements