package com.studentevaluator.bdd;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Simple test to verify BDD setup and Spring Boot integration
 */
@SpringBootTest
@ActiveProfiles("test")
public class BDDSetupTest {

    @Test
    public void contextLoads() {
        // This test verifies that the Spring Boot context loads correctly
        // for BDD tests, which is essential for Cucumber-Spring integration
        assertTrue(true, "Spring Boot context should load successfully");
    }

    @Test
    public void cucumberDependenciesAvailable() {
        // Verify that Cucumber classes are available on the classpath
        try {
            Class.forName("io.cucumber.java.en.Given");
            Class.forName("io.cucumber.spring.CucumberContextConfiguration");
            Class.forName("io.cucumber.junit.platform.engine.Constants");
            assertTrue(true, "All required Cucumber dependencies are available");
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Required Cucumber dependencies are missing: " + e.getMessage());
        }
    }
}