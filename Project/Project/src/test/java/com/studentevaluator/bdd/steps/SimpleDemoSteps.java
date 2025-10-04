package com.studentevaluator.bdd.steps;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Simple demo step definitions to verify BDD framework setup
 */
public class SimpleDemoSteps {

    private boolean frameworkConfigured = false;
    private boolean testExecuted = false;

    @Given("the BDD framework is configured")
    public void theBDDFrameworkIsConfigured() {
        frameworkConfigured = true;
    }

    @When("I run a simple test")
    public void iRunASimpleTest() {
        testExecuted = true;
    }

    @Then("the test should pass successfully")
    public void theTestShouldPassSuccessfully() {
        assertTrue(frameworkConfigured, "BDD framework should be configured");
        assertTrue(testExecuted, "Test should have been executed");
    }
}