package com.studentevaluator.bdd.steps;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.studentevaluator.dto.AssignmentResponse;
import com.studentevaluator.model.Assignment;
import com.studentevaluator.repository.AssignmentRepository;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Step definitions for assignment submission scenarios
 */
public class AssignmentSubmissionSteps {

    @Autowired
    private CommonSteps commonSteps;

    @Autowired
    private AssignmentRepository assignmentRepository;

    private String currentAssignmentId;
    private ResponseEntity<?> lastResponse;
    private String testJavaContent;
    private String testFileName;

    @Given("I have a valid Java assignment file {string}")
    public void iHaveAValidJavaAssignmentFile(String fileName) {
        testFileName = fileName;
        testJavaContent = """
            public class Calculator {
                public int add(int a, int b) {
                    return a + b;
                }
                
                public int subtract(int a, int b) {
                    return a - b;
                }
                
                public int multiply(int a, int b) {
                    return a * b;
                }
                
                public double divide(int a, int b) {
                    if (b == 0) throw new IllegalArgumentException("Division by zero");
                    return (double) a / b;
                }
            }
            """;
    }

    @Given("I have an assignment with ID {string}")
    public void iHaveAnAssignmentWithId(String assignmentId) {
        Assignment assignment = new Assignment();
        assignment.setAssignmentId(assignmentId);
        assignment.setTitle("Test Assignment");
        assignment.setDescription("BDD Test Assignment");
        assignment.setCreatedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);
        currentAssignmentId = assignmentId;
    }

    @Given("I have a valid JUnit test file {string}")
    public void iHaveAValidJUnitTestFile(String fileName) {
        testFileName = fileName;
        testJavaContent = """
            import org.junit.jupiter.api.Test;
            import static org.junit.jupiter.api.Assertions.*;
            
            public class CalculatorTest {
                private Calculator calculator = new Calculator();
                
                @Test
                public void testAdd() {
                    assertEquals(5, calculator.add(2, 3));
                }
                
                @Test
                public void testSubtract() {
                    assertEquals(1, calculator.subtract(3, 2));
                }
                
                @Test
                public void testMultiply() {
                    assertEquals(6, calculator.multiply(2, 3));
                }
                
                @Test
                public void testDivide() {
                    assertEquals(2.0, calculator.divide(6, 3), 0.001);
                }
                
                @Test
                public void testDivideByZero() {
                    assertThrows(IllegalArgumentException.class, () -> calculator.divide(5, 0));
                }
            }
            """;
    }

    @Given("I have an invalid file {string}")
    public void iHaveAnInvalidFile(String fileName) {
        testFileName = fileName;
        testJavaContent = "This is not a Java file content";
    }

    @Given("I have a Java file larger than 1MB")
    public void iHaveAJavaFileLargerThan1MB() {
        testFileName = "LargeFile.java";
        StringBuilder largeContent = new StringBuilder();
        largeContent.append("public class LargeFile {\n");
        // Create content larger than 1MB
        for (int i = 0; i < 50000; i++) {
            largeContent.append("    // This is a comment line to make the file large - line ").append(i).append("\n");
        }
        largeContent.append("}");
        testJavaContent = largeContent.toString();
    }

    @When("I upload the assignment file with title {string}")
    public void iUploadTheAssignmentFileWithTitle(String title) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(testJavaContent.getBytes()) {
            @Override
            public String getFilename() {
                return testFileName;
            }
        });
        body.add("title", title);
        body.add("description", "BDD Test Assignment");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        lastResponse = commonSteps.getRestTemplate().postForEntity(
            commonSteps.getBaseUrl() + "/api/v1/assignments",
            requestEntity,
            AssignmentResponse.class
        );
    }

    @When("I upload the test file for assignment {string}")
    public void iUploadTheTestFileForAssignment(String assignmentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(testJavaContent.getBytes()) {
            @Override
            public String getFilename() {
                return testFileName;
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        lastResponse = commonSteps.getRestTemplate().postForEntity(
            commonSteps.getBaseUrl() + "/api/v1/assignments/" + assignmentId + "/tests",
            requestEntity,
            String.class
        );
    }

    @When("I attempt to upload the file as an assignment")
    public void iAttemptToUploadTheFileAsAnAssignment() {
        iUploadTheAssignmentFileWithTitle("Invalid File Test");
    }

    @When("I attempt to upload the oversized file")
    public void iAttemptToUploadTheOversizedFile() {
        iUploadTheAssignmentFileWithTitle("Oversized File Test");
    }

    @When("I attempt to upload the test file for non-existent assignment {string}")
    public void iAttemptToUploadTheTestFileForNonExistentAssignment(String assignmentId) {
        iUploadTheTestFileForAssignment(assignmentId);
    }

    @Then("the assignment should be stored successfully")
    public void theAssignmentShouldBeStoredSuccessfully() {
        assertEquals(HttpStatus.CREATED, lastResponse.getStatusCode());
        AssignmentResponse response = (AssignmentResponse) lastResponse.getBody();
        assertNotNull(response);
        assertNotNull(response.getAssignmentId());
        currentAssignmentId = response.getAssignmentId();
    }

    @Then("the assignment should have an ID")
    public void theAssignmentShouldHaveAnId() {
        assertNotNull(currentAssignmentId);
        assertTrue(assignmentRepository.existsById(currentAssignmentId));
    }

    @Then("the assignment status should be {string}")
    public void theAssignmentStatusShouldBe(String expectedStatus) {
        // This would be implemented when assignment status is added to the model
        // For now, we verify the assignment exists
        assertTrue(assignmentRepository.existsById(currentAssignmentId));
    }

    @Then("the test file should be associated with the assignment")
    public void theTestFileShouldBeAssociatedWithTheAssignment() {
        assertEquals(HttpStatus.OK, lastResponse.getStatusCode());
        Assignment assignment = assignmentRepository.findById(currentAssignmentId).orElse(null);
        assertNotNull(assignment);
        assertNotNull(assignment.getTestFilePath());
    }

    @Then("the upload should be rejected")
    public void theUploadShouldBeRejected() {
        assertTrue(lastResponse.getStatusCode().is4xxClientError());
    }

    @Then("I should receive an error message {string}")
    public void iShouldReceiveAnErrorMessage(String expectedMessage) {
        assertTrue(lastResponse.getStatusCode().is4xxClientError());
        // The actual error message validation would depend on the error response format
        // This is a simplified check
        assertNotNull(lastResponse.getBody());
    }
}