# Student Evaluator System - CLI Usage Guide

The Student Evaluator System provides a command-line interface (CLI) for interacting with the evaluation system. The CLI allows you to submit assignments, upload test files, trigger evaluations, and retrieve results.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- The Student Evaluator System REST API running (default: http://localhost:8080)

## Building the CLI

```bash
mvn clean compile
```

## Running the CLI

The CLI can be run using Maven:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="<command> <options>"
```

Or by running the compiled JAR:

```bash
java -jar target/student-evaluator-system-1.0.0.jar <command> <options>
```

## Available Commands

### 1. Submit Assignment (`submit`)

Upload an assignment file to the system.

**Usage:**
```bash
evaluator submit <assignment-file> --assignment-id <id> --title <title> [--description <desc>]
```

**Parameters:**
- `<assignment-file>`: Path to the Java assignment file
- `--assignment-id, -i`: Unique identifier for the assignment
- `--title, -t`: Title of the assignment
- `--description, -d`: Optional description of the assignment

**Example:**
```bash
evaluator submit Calculator.java -i calc-001 -t "Basic Calculator" -d "Implement basic arithmetic operations"
```

### 2. Upload Test Files (`upload-tests`)

Upload JUnit test files and associate them with an assignment.

**Usage:**
```bash
evaluator upload-tests <test-file> --assignment <assignment-id>
```

**Parameters:**
- `<test-file>`: Path to the JUnit test file
- `--assignment, -a`: Assignment ID to associate the test file with

**Example:**
```bash
evaluator upload-tests CalculatorTest.java -a calc-001
```

### 3. Evaluate Submission (`evaluate`)

Trigger evaluation of a student submission against an assignment.

**Usage:**
```bash
evaluator evaluate <submission-file> --student <student-id> --assignment <assignment-id> [--verbose]
```

**Parameters:**
- `<submission-file>`: Path to the student's Java submission file
- `--student, -s`: Student identifier
- `--assignment, -a`: Assignment identifier
- `--verbose, -v`: Show detailed evaluation results

**Example:**
```bash
evaluator evaluate StudentCalculator.java -s student123 -a calc-001 --verbose
```

### 4. Get Results (`results`)

Retrieve and display evaluation results.

**Usage:**
```bash
# Get student scores
evaluator results --student <student-id> [--verbose]

# Get student evaluation history
evaluator results --student <student-id> --history [--verbose]

# Get specific evaluation result
evaluator results --evaluation <evaluation-id> [--verbose]
```

**Parameters:**
- `--student, -s`: Student ID to get results for
- `--evaluation, -e`: Evaluation ID to get specific result
- `--history, -h`: Show evaluation history for student
- `--verbose, -v`: Show detailed results

**Examples:**
```bash
# Get current scores for a student
evaluator results -s student123

# Get evaluation history for a student
evaluator results -s student123 --history

# Get specific evaluation result
evaluator results -e eval-456 --verbose
```

## Configuration

The CLI connects to the REST API using the base URL configured in `application.yml`:

```yaml
cli:
  api:
    base-url: http://localhost:8080
```

You can override this using environment variables:
```bash
export CLI_API_BASE_URL=http://your-server:8080
```

## Complete Workflow Example

Here's a complete example of using the CLI to set up and evaluate an assignment:

```bash
# 1. Submit the assignment template
evaluator submit Calculator.java -i calc-001 -t "Basic Calculator" -d "Implement arithmetic operations"

# 2. Upload test files for the assignment
evaluator upload-tests CalculatorTest.java -a calc-001

# 3. Evaluate a student submission
evaluator evaluate StudentCalculator.java -s student123 -a calc-001 --verbose

# 4. Check the results
evaluator results -s student123 --verbose

# 5. View evaluation history
evaluator results -s student123 --history
```

## Error Handling

The CLI provides clear error messages for common issues:

- **File not found**: Verify the file path is correct
- **Invalid file type**: Only `.java` files are accepted
- **API connection errors**: Check that the REST API is running
- **Assignment not found**: Verify the assignment ID exists
- **Student not found**: Check the student ID is correct

## Exit Codes

- `0`: Success
- `1`: General error (file not found, invalid arguments, API error)
- `2`: Evaluation failed (compilation or test failures)

## Help

Use the `--help` option with any command to see detailed usage information:

```bash
evaluator --help
evaluator submit --help
evaluator evaluate --help
evaluator results --help
```

## Troubleshooting

### Common Issues

1. **"Connection refused" errors**
   - Ensure the REST API is running on the configured port
   - Check the `cli.api.base-url` configuration

2. **"File not found" errors**
   - Verify file paths are correct and files exist
   - Ensure files have `.java` extension

3. **"Assignment not found" errors**
   - Submit the assignment first using the `submit` command
   - Verify the assignment ID is correct

4. **Permission errors**
   - Ensure you have read permissions for input files
   - Check write permissions for temporary directories

### Debug Mode

For detailed debugging information, run with verbose logging:

```bash
java -Dlogging.level.com.studentevaluator=DEBUG -jar target/student-evaluator-system-1.0.0.jar <command>
```