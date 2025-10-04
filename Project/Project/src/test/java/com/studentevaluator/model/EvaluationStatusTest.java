package com.studentevaluator.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EvaluationStatus Enum Tests")
class EvaluationStatusTest {
    
    @Test
    @DisplayName("Should have all expected enum values")
    void shouldHaveAllExpectedEnumValues() {
        // Given & When
        EvaluationStatus[] values = EvaluationStatus.values();
        
        // Then
        assertEquals(4, values.length);
        assertTrue(containsStatus(values, EvaluationStatus.PENDING));
        assertTrue(containsStatus(values, EvaluationStatus.IN_PROGRESS));
        assertTrue(containsStatus(values, EvaluationStatus.COMPLETED));
        assertTrue(containsStatus(values, EvaluationStatus.FAILED));
    }
    
    @Test
    @DisplayName("Should have correct descriptions for each status")
    void shouldHaveCorrectDescriptionsForEachStatus() {
        // Then
        assertEquals("Evaluation is pending", EvaluationStatus.PENDING.getDescription());
        assertEquals("Evaluation is in progress", EvaluationStatus.IN_PROGRESS.getDescription());
        assertEquals("Evaluation completed successfully", EvaluationStatus.COMPLETED.getDescription());
        assertEquals("Evaluation failed", EvaluationStatus.FAILED.getDescription());
    }
    
    @Test
    @DisplayName("Should convert from string correctly")
    void shouldConvertFromStringCorrectly() {
        // Then
        assertEquals(EvaluationStatus.PENDING, EvaluationStatus.valueOf("PENDING"));
        assertEquals(EvaluationStatus.IN_PROGRESS, EvaluationStatus.valueOf("IN_PROGRESS"));
        assertEquals(EvaluationStatus.COMPLETED, EvaluationStatus.valueOf("COMPLETED"));
        assertEquals(EvaluationStatus.FAILED, EvaluationStatus.valueOf("FAILED"));
    }
    
    @Test
    @DisplayName("Should throw exception for invalid string")
    void shouldThrowExceptionForInvalidString() {
        // Then
        assertThrows(IllegalArgumentException.class, () -> {
            EvaluationStatus.valueOf("INVALID_STATUS");
        });
    }
    
    @Test
    @DisplayName("Should maintain enum order")
    void shouldMaintainEnumOrder() {
        // Given
        EvaluationStatus[] values = EvaluationStatus.values();
        
        // Then
        assertEquals(EvaluationStatus.PENDING, values[0]);
        assertEquals(EvaluationStatus.IN_PROGRESS, values[1]);
        assertEquals(EvaluationStatus.COMPLETED, values[2]);
        assertEquals(EvaluationStatus.FAILED, values[3]);
    }
    
    @Test
    @DisplayName("Should support ordinal values")
    void shouldSupportOrdinalValues() {
        // Then
        assertEquals(0, EvaluationStatus.PENDING.ordinal());
        assertEquals(1, EvaluationStatus.IN_PROGRESS.ordinal());
        assertEquals(2, EvaluationStatus.COMPLETED.ordinal());
        assertEquals(3, EvaluationStatus.FAILED.ordinal());
    }
    
    @Test
    @DisplayName("Should support name method")
    void shouldSupportNameMethod() {
        // Then
        assertEquals("PENDING", EvaluationStatus.PENDING.name());
        assertEquals("IN_PROGRESS", EvaluationStatus.IN_PROGRESS.name());
        assertEquals("COMPLETED", EvaluationStatus.COMPLETED.name());
        assertEquals("FAILED", EvaluationStatus.FAILED.name());
    }
    
    private boolean containsStatus(EvaluationStatus[] values, EvaluationStatus status) {
        for (EvaluationStatus value : values) {
            if (value == status) {
                return true;
            }
        }
        return false;
    }
}