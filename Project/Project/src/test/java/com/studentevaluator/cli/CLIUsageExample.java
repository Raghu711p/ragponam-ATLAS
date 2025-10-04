package com.studentevaluator.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example demonstrating CLI usage and help output.
 */
class CLIUsageExample {
    
    @Test
    void testMainCommandHelp() {
        // Capture output
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));
        
        try {
            // Create CLI and show help
            EvaluatorCLI cli = new EvaluatorCLI(new TestFactory());
            CommandLine cmd = new CommandLine(cli, new TestFactory());
            
            // Execute help command
            int exitCode = cmd.execute("--help");
            
            // Verify
            assertEquals(0, exitCode);
            String output = out.toString();
            assertTrue(output.contains("Student Evaluator System CLI"));
            assertTrue(output.contains("submit"));
            assertTrue(output.contains("upload-tests"));
            assertTrue(output.contains("evaluate"));
            assertTrue(output.contains("results"));
            
        } finally {
            System.setOut(originalOut);
        }
    }
    
    @Test
    void testCLIStructure() {
        // Test that CLI can be created and has expected subcommands
        EvaluatorCLI cli = new EvaluatorCLI(new TestFactory());
        CommandLine cmd = new CommandLine(cli, new TestFactory());
        
        // Verify subcommands are registered
        assertTrue(cmd.getSubcommands().containsKey("submit"));
        assertTrue(cmd.getSubcommands().containsKey("upload-tests"));
        assertTrue(cmd.getSubcommands().containsKey("evaluate"));
        assertTrue(cmd.getSubcommands().containsKey("results"));
        
        // Verify command descriptions
        assertEquals("Submit an assignment file for evaluation setup", 
                cmd.getSubcommands().get("submit").getCommandSpec().usageMessage().description()[0]);
        assertEquals("Upload JUnit test files and associate them with an assignment", 
                cmd.getSubcommands().get("upload-tests").getCommandSpec().usageMessage().description()[0]);
        assertEquals("Trigger evaluation of a student submission against an assignment", 
                cmd.getSubcommands().get("evaluate").getCommandSpec().usageMessage().description()[0]);
        assertEquals("Retrieve and display evaluation results for students", 
                cmd.getSubcommands().get("results").getCommandSpec().usageMessage().description()[0]);
    }
    
    /**
     * Test factory for Picocli that creates instances without Spring.
     */
    private static class TestFactory implements CommandLine.IFactory {
        @Override
        public <K> K create(Class<K> cls) throws Exception {
            return cls.getDeclaredConstructor().newInstance();
        }
    }
}