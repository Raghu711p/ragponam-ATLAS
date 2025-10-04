package com.studentevaluator.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple test to verify that repository interfaces compile correctly
 * and can be loaded by Spring context.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class RepositoryCompilationTest {
    
    @Test
    void testRepositoryInterfacesCompile() {
        // This test verifies that all repository interfaces compile correctly
        // and their method signatures are valid
        
        // Check that repository interfaces exist and have expected methods
        assertThat(StudentRepository.class).isNotNull();
        assertThat(AssignmentRepository.class).isNotNull();
        assertThat(EvaluationRepository.class).isNotNull();
        assertThat(DynamoDBRepository.class).isNotNull();
        
        // Verify key methods exist by checking they're declared
        assertThat(StudentRepository.class.getMethods())
                .extracting("name")
                .contains("findByEmail", "findByNameContainingIgnoreCase", "existsByEmail");
        
        assertThat(AssignmentRepository.class.getMethods())
                .extracting("name")
                .contains("findByTitle", "updateTestFilePath", "hasTestFile");
        
        assertThat(EvaluationRepository.class.getMethods())
                .extracting("name")
                .contains("findByStudentId", "findByAssignmentId", "getAverageScoreByStudent");
        
        assertThat(DynamoDBRepository.class.getMethods())
                .extracting("name")
                .contains("storeEvaluationLog", "getEvaluationLogs", "storeCompilationLog");
    }
}