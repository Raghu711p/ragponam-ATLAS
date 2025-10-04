package com.studentevaluator.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CompilationResult Model Tests")
class CompilationResultTest {
    
    @Test
    @DisplayName("Should create successful compilation result")
    void shouldCreateSuccessfulCompilationResult() {
        // Given
        String output = "Compilation successful";
        String compiledClassPath = "/tmp/compiled/MyClass.class";
        
        // When
        CompilationResult result = new CompilationResult(true, output, compiledClassPath);
        
        // Then
        assertTrue(result.isSuccessful());
        assertEquals(output, result.getOutput());
        assertEquals(compiledClassPath, result.getCompiledClassPath());
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().isEmpty());
        assertFalse(result.hasErrors());
    }
    
    @Test
    @DisplayName("Should create failed compilation result")
    void shouldCreateFailedCompilationResult() {
        // Given
        String output = "Compilation failed";
        List<String> errors = Arrays.asList(
            "cannot find symbol: variable x",
            "';' expected"
        );
        
        // When
        CompilationResult result = new CompilationResult(false, output, errors);
        
        // Then
        assertFalse(result.isSuccessful());
        assertEquals(output, result.getOutput());
        assertNull(result.getCompiledClassPath());
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getErrors().contains("cannot find symbol: variable x"));
        assertTrue(result.getErrors().contains("';' expected"));
        assertTrue(result.hasErrors());
    }
    
    @Test
    @DisplayName("Should create compilation result with all fields")
    void shouldCreateCompilationResultWithAllFields() {
        // Given
        String output = "Compilation completed with warnings";
        List<String> errors = Arrays.asList("Warning: unused variable");
        String compiledClassPath = "/tmp/compiled/MyClass.class";
        
        // When
        CompilationResult result = new CompilationResult(true, output, errors, compiledClassPath);
        
        // Then
        assertTrue(result.isSuccessful());
        assertEquals(output, result.getOutput());
        assertEquals(compiledClassPath, result.getCompiledClassPath());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().contains("Warning: unused variable"));
        assertTrue(result.hasErrors());
    }
    
    @Test
    @DisplayName("Should handle null errors list")
    void shouldHandleNullErrorsList() {
        // When
        CompilationResult result = new CompilationResult(true, "Success", null, "/path/to/class");
        
        // Then
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().isEmpty());
        assertFalse(result.hasErrors());
    }
    
    @Test
    @DisplayName("Should add error to existing errors list")
    void shouldAddErrorToExistingErrorsList() {
        // Given
        CompilationResult result = new CompilationResult();
        
        // When
        result.addError("First error");
        result.addError("Second error");
        
        // Then
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getErrors().contains("First error"));
        assertTrue(result.getErrors().contains("Second error"));
        assertTrue(result.hasErrors());
    }
    
    @Test
    @DisplayName("Should initialize errors list when adding to null list")
    void shouldInitializeErrorsListWhenAddingToNullList() {
        // Given
        CompilationResult result = new CompilationResult();
        result.setErrors(null);
        
        // When
        result.addError("New error");
        
        // Then
        assertNotNull(result.getErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().contains("New error"));
    }
    
    @Test
    @DisplayName("Should set errors list with defensive copy")
    void shouldSetErrorsListWithDefensiveCopy() {
        // Given
        List<String> originalErrors = new ArrayList<>(Arrays.asList("Error 1", "Error 2"));
        CompilationResult result = new CompilationResult();
        
        // When
        result.setErrors(originalErrors);
        originalErrors.clear(); // Modify original list
        
        // Then
        assertEquals(2, result.getErrors().size()); // Should not be affected
        assertTrue(result.getErrors().contains("Error 1"));
        assertTrue(result.getErrors().contains("Error 2"));
    }
    
    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        List<String> errors = Arrays.asList("Error 1");
        CompilationResult result1 = new CompilationResult(true, "Success", errors, "/path/class");
        CompilationResult result2 = new CompilationResult(true, "Success", errors, "/path/class");
        CompilationResult result3 = new CompilationResult(false, "Failed", errors, null);
        
        // Then
        assertEquals(result1, result2);
        assertNotEquals(result1, result3);
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1.hashCode(), result3.hashCode());
    }
    
    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        List<String> errors = Arrays.asList("Syntax error");
        CompilationResult result = new CompilationResult(false, "Failed", errors, null);
        
        // When
        String toString = result.toString();
        
        // Then
        assertTrue(toString.contains("successful=false"));
        assertTrue(toString.contains("Failed"));
        assertTrue(toString.contains("Syntax error"));
    }
    
    @Test
    @DisplayName("Should handle empty errors list correctly")
    void shouldHandleEmptyErrorsListCorrectly() {
        // Given
        CompilationResult result = new CompilationResult();
        
        // Then
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().isEmpty());
        assertFalse(result.hasErrors());
    }
}