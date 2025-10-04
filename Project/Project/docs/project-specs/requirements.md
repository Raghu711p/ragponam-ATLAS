# Requirements Document

## Introduction

The Student Evaluator System is a personal learning project designed to automate the evaluation of student Java programming assignments through JUnit testing. The system will provide a RESTful API with CLI interface, compile and test Java submissions, and store results in both MySQL (structured data) and DynamoDB (unstructured logs). This project serves as a comprehensive learning exercise covering Java development, testing frameworks, database integration, and DevOps practices.

## Requirements

### Requirement 1

**User Story:** As a system administrator, I want to upload Java assignment files and their corresponding JUnit test files, so that I can set up automated evaluation for student submissions.

#### Acceptance Criteria

1. WHEN a user uploads a Java file THEN the system SHALL accept and store the file for evaluation
2. WHEN a user uploads a JUnit test file THEN the system SHALL associate it with a specific assignment
3. WHEN test files are uploaded THEN the system SHALL validate that they are properly formatted JUnit tests
4. WHEN assignments are submitted THEN the system SHALL store them for future reference and re-evaluation

### Requirement 2

**User Story:** As a system administrator, I want the system to automatically compile and test student submissions, so that I can get consistent and objective evaluation results.

#### Acceptance Criteria

1. WHEN a Java file is submitted for evaluation THEN the system SHALL compile the file using available JDK
2. WHEN compilation is successful THEN the system SHALL run associated JUnit tests against the compiled code
3. WHEN tests are executed THEN the system SHALL determine pass/fail results based on test outcomes
4. WHEN compilation fails THEN the system SHALL capture error messages and provide meaningful feedback
5. WHEN the evaluation process completes THEN the system SHALL store both results and detailed logs

### Requirement 3

**User Story:** As a system administrator, I want evaluation results stored in appropriate databases, so that I can maintain both structured scores and detailed execution logs.

#### Acceptance Criteria

1. WHEN evaluation completes THEN the system SHALL store structured results (scores, timestamps, status) in MySQL
2. WHEN evaluation runs THEN the system SHALL store unstructured logs (compilation output, test execution details, errors) in DynamoDB
3. WHEN storing results THEN the system SHALL maintain data integrity between MySQL and DynamoDB
4. WHEN student results are stored THEN the system SHALL implement a HashMap mapping student hashcodes to scores for quick lookup

### Requirement 4

**User Story:** As a developer or administrator, I want to interact with the system through both API and CLI interfaces, so that I can integrate it with other tools or use it directly.

#### Acceptance Criteria

1. WHEN accessing the system THEN the system SHALL provide RESTful API endpoints for all operations
2. WHEN using the API THEN the system SHALL support assignment upload, test case upload, evaluation triggering, and result retrieval
3. WHEN using the CLI THEN the system SHALL provide commands for assignment submission, test upload, evaluation, and result retrieval
4. WHEN operations fail THEN the system SHALL return clear error messages through both API and CLI
5. WHEN using the CLI THEN the system SHALL provide clear feedback on all operations performed

### Requirement 5

**User Story:** As a developer, I want the system to handle various data entities and their relationships, so that I can track students, assignments, and evaluations systematically.

#### Acceptance Criteria

1. WHEN managing students THEN the system SHALL store student ID, name, and other relevant attributes
2. WHEN managing assignments THEN the system SHALL store assignment ID, description, submission date, and associated test information
3. WHEN managing evaluations THEN the system SHALL store evaluation ID, student ID, assignment ID, score, timestamp, and status
4. WHEN storing data THEN the system SHALL maintain proper foreign key relationships between entities

### Requirement 6

**User Story:** As a system user, I want the system to perform efficiently and reliably, so that I can evaluate assignments quickly and trust the results.

#### Acceptance Criteria

1. WHEN evaluating a standard Java assignment THEN the system SHALL complete compilation and testing within 30 seconds
2. WHEN receiving API requests THEN the system SHALL respond within 5 seconds under normal load
3. WHEN compilation errors occur THEN the system SHALL handle them gracefully and provide meaningful feedback
4. WHEN processing inputs THEN the system SHALL validate all inputs to prevent code injection attacks

### Requirement 7

**User Story:** As a developer, I want the system to support modern development practices, so that I can demonstrate comprehensive software engineering skills.

#### Acceptance Criteria

1. WHEN developing the system THEN the system SHALL implement automated BDD scenarios for assignment evaluation
2. WHEN building the system THEN the system SHALL include CI/CD pipelines with automated test execution
3. WHEN deploying the system THEN the system SHALL support demonstration deployment
4. WHEN maintaining the system THEN the system SHALL be designed with clear separation of concerns and appropriate documentation