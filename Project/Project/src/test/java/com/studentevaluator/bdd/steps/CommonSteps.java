package com.studentevaluator.bdd.steps;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.studentevaluator.model.Assignment;
import com.studentevaluator.model.Student;
import com.studentevaluator.repository.AssignmentRepository;
import com.studentevaluator.repository.EvaluationRepository;
import com.studentevaluator.repository.StudentRepository;

import io.cucumber.java.en.Given;

/**
 * Common step definitions shared across multiple features
 */
public class CommonSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private EvaluationRepository evaluationRepository;

    @LocalServerPort
    private int port;

    private String baseUrl;

    @Given("the system is running")
    public void theSystemIsRunning() {
        baseUrl = "http://localhost:" + port;
        // Verify system is responsive
        String healthResponse = restTemplate.getForObject(baseUrl + "/actuator/health", String.class);
        assert healthResponse != null && healthResponse.contains("UP");
    }

    @Given("the database is clean")
    public void theDatabaseIsClean() {
        evaluationRepository.deleteAll();
        assignmentRepository.deleteAll();
        studentRepository.deleteAll();
    }

    @Given("I have a student {string} registered in the system")
    public void iHaveAStudentRegisteredInTheSystem(String studentId) {
        Student student = new Student();
        student.setStudentId(studentId);
        student.setName(studentId.replace("-", " ").toUpperCase());
        student.setEmail(studentId + "@test.com");
        student.setCreatedAt(LocalDateTime.now());
        studentRepository.save(student);
    }

    @Given("I have students {string}, {string}, {string} registered in the system")
    public void iHaveMultipleStudentsRegisteredInTheSystem(String student1, String student2, String student3) {
        iHaveAStudentRegisteredInTheSystem(student1);
        iHaveAStudentRegisteredInTheSystem(student2);
        iHaveAStudentRegisteredInTheSystem(student3);
    }

    @Given("I have an assignment {string} with test files ready")
    public void iHaveAnAssignmentWithTestFilesReady(String assignmentId) {
        Assignment assignment = new Assignment();
        assignment.setAssignmentId(assignmentId);
        assignment.setTitle("Test Assignment");
        assignment.setDescription("BDD Test Assignment");
        assignment.setTestFilePath("/test/path/TestFile.java");
        assignment.setCreatedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public TestRestTemplate getRestTemplate() {
        return restTemplate;
    }
}