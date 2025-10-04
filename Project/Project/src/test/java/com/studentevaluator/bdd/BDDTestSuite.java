package com.studentevaluator.bdd;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * BDD Test Suite that runs all Cucumber-based behavior-driven development tests
 * This suite includes all feature files and their corresponding step definitions
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CucumberTestRunner.class
})
public class BDDTestSuite {
    // This class serves as a test suite aggregator for all BDD tests
    // The actual test execution is handled by CucumberTestRunner
}