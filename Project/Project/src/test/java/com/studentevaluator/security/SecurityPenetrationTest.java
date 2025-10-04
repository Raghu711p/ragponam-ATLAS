package com.studentevaluator.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentevaluator.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Penetration testing scenarios for security validation.
 * Tests various attack vectors and security vulnerabilities.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class SecurityPenetrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testPathTraversalAttack_ShouldBeBlocked() throws Exception {
        // Test path traversal in assignment ID
        mockMvc.perform(get("/api/v1/evaluations/assignments/../../etc/passwd"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
    
    @Test
    void testMaliciousFileUpload_ShouldBeBlocked() throws Exception {
        // Test malicious Java file with dangerous code
        String maliciousContent = 
            "import java.io.*;\n" +
            "public class Malicious {\n" +
            "    public static void main(String[] args) {\n" +
            "        try {\n" +
            "            Runtime.getRuntime().exec(\"rm -rf /\");\n" +
            "        } catch (Exception e) {}\n" +
            "    }\n" +
            "}";
        
        MockMultipartFile maliciousFile = new MockMultipartFile(
            "file", "Malicious.java", "text/plain", maliciousContent.getBytes());
        
        MvcResult result = mockMvc.perform(multipart("/api/v1/evaluations/assignments")
                .file(maliciousFile)
                .param("assignmentId", "test123")
                .param("title", "Test Assignment"))
                .andExpect(status().isBadRequest())
                .andReturn();
        
        String responseContent = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(responseContent, ErrorResponse.class);
        assertTrue(errorResponse.getError().getMessage().contains("dangerous code"));
    }
    
    @Test
    void testSQLInjectionAttempt_ShouldBeBlocked() throws Exception {
        // Test SQL injection in assignment ID
        String sqlInjection = "'; DROP TABLE assignments; --";
        
        mockMvc.perform(get("/api/v1/evaluations/assignments/" + sqlInjection))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
    
    @Test
    void testXSSAttempt_ShouldBeBlocked() throws Exception {
        // Test XSS in assignment title
        String xssPayload = "<script>alert('xss')</script>";
        MockMultipartFile validFile = new MockMultipartFile(
            "file", "Test.java", "text/plain", "public class Test {}".getBytes());
        
        mockMvc.perform(multipart("/api/v1/evaluations/assignments")
                .file(validFile)
                .param("assignmentId", "test123")
                .param("title", xssPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
    
    @Test
    void testOversizedFileUpload_ShouldBeBlocked() throws Exception {
        // Test file size limit
        byte[] oversizedContent = new byte[2 * 1024 * 1024]; // 2MB
        MockMultipartFile oversizedFile = new MockMultipartFile(
            "file", "Large.java", "text/plain", oversizedContent);
        
        mockMvc.perform(multipart("/api/v1/evaluations/assignments")
                .file(oversizedFile)
                .param("assignmentId", "test123")
                .param("title", "Test Assignment"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testInvalidFileExtension_ShouldBeBlocked() throws Exception {
        // Test non-Java file upload
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file", "malicious.exe", "application/octet-stream", "malicious content".getBytes());
        
        mockMvc.perform(multipart("/api/v1/evaluations/assignments")
                .file(invalidFile)
                .param("assignmentId", "test123")
                .param("title", "Test Assignment"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
    
    @Test
    void testReflectionAttack_ShouldBeBlocked() throws Exception {
        // Test Java reflection attack
        String reflectionContent = 
            "import java.lang.reflect.*;\n" +
            "public class ReflectionAttack {\n" +
            "    public void attack() {\n" +
            "        try {\n" +
            "            Class<?> clazz = Class.forName(\"java.lang.Runtime\");\n" +
            "            Method method = clazz.getMethod(\"exec\", String.class);\n" +
            "        } catch (Exception e) {}\n" +
            "    }\n" +
            "}";
        
        MockMultipartFile reflectionFile = new MockMultipartFile(
            "file", "ReflectionAttack.java", "text/plain", reflectionContent.getBytes());
        
        mockMvc.perform(multipart("/api/v1/evaluations/assignments")
                .file(reflectionFile)
                .param("assignmentId", "test123")
                .param("title", "Test Assignment"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
    
    @Test
    void testFileSystemAttack_ShouldBeBlocked() throws Exception {
        // Test file system access attack
        String fileSystemContent = 
            "import java.io.*;\n" +
            "public class FileSystemAttack {\n" +
            "    public void attack() {\n" +
            "        try {\n" +
            "            FileInputStream fis = new FileInputStream(\"/etc/passwd\");\n" +
            "        } catch (Exception e) {}\n" +
            "    }\n" +
            "}";
        
        MockMultipartFile fileSystemFile = new MockMultipartFile(
            "file", "FileSystemAttack.java", "text/plain", fileSystemContent.getBytes());
        
        mockMvc.perform(multipart("/api/v1/evaluations/assignments")
                .file(fileSystemFile)
                .param("assignmentId", "test123")
                .param("title", "Test Assignment"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
    
    @Test
    void testNetworkAttack_ShouldBeBlocked() throws Exception {
        // Test network access attack
        String networkContent = 
            "import java.net.*;\n" +
            "public class NetworkAttack {\n" +
            "    public void attack() {\n" +
            "        try {\n" +
            "            Socket socket = new Socket(\"malicious.com\", 80);\n" +
            "        } catch (Exception e) {}\n" +
            "    }\n" +
            "}";
        
        MockMultipartFile networkFile = new MockMultipartFile(
            "file", "NetworkAttack.java", "text/plain", networkContent.getBytes());
        
        mockMvc.perform(multipart("/api/v1/evaluations/assignments")
                .file(networkFile)
                .param("assignmentId", "test123")
                .param("title", "Test Assignment"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
    
    @Test
    void testScriptEngineAttack_ShouldBeBlocked() throws Exception {
        // Test script engine attack
        String scriptContent = 
            "import javax.script.*;\n" +
            "public class ScriptAttack {\n" +
            "    public void attack() {\n" +
            "        try {\n" +
            "            ScriptEngineManager manager = new ScriptEngineManager();\n" +
            "            ScriptEngine engine = manager.getEngineByName(\"javascript\");\n" +
            "        } catch (Exception e) {}\n" +
            "    }\n" +
            "}";
        
        MockMultipartFile scriptFile = new MockMultipartFile(
            "file", "ScriptAttack.java", "text/plain", scriptContent.getBytes());
        
        mockMvc.perform(multipart("/api/v1/evaluations/assignments")
                .file(scriptFile)
                .param("assignmentId", "test123")
                .param("title", "Test Assignment"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
    
    @Test
    void testRateLimitingBypass_ShouldBeBlocked() throws Exception {
        // Test rate limiting with multiple IPs (simulated)
        MockMultipartFile validFile = new MockMultipartFile(
            "file", "Test.java", "text/plain", "public class Test {}".getBytes());
        
        // Make multiple requests rapidly
        for (int i = 0; i < 70; i++) { // Exceed rate limit
            try {
                mockMvc.perform(multipart("/api/v1/evaluations/assignments")
                        .file(validFile)
                        .param("assignmentId", "test" + i)
                        .param("title", "Test Assignment " + i)
                        .header("X-Forwarded-For", "192.168.1." + (i % 10)));
            } catch (Exception e) {
                // Some requests should be rate limited
            }
        }
        
        // The last request should be rate limited
        mockMvc.perform(multipart("/api/v1/evaluations/assignments")
                .file(validFile)
                .param("assignmentId", "testFinal")
                .param("title", "Final Test"))
                .andExpect(status().isTooManyRequests());
    }
    
    @Test
    void testValidRequest_ShouldPass() throws Exception {
        // Test that valid requests still work
        String validContent = 
            "public class Calculator {\n" +
            "    public int add(int a, int b) {\n" +
            "        return a + b;\n" +
            "    }\n" +
            "}";
        
        MockMultipartFile validFile = new MockMultipartFile(
            "file", "Calculator.java", "text/plain", validContent.getBytes());
        
        mockMvc.perform(multipart("/api/v1/evaluations/assignments")
                .file(validFile)
                .param("assignmentId", "calc123")
                .param("title", "Calculator Assignment")
                .param("description", "Simple calculator implementation"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignmentId").value("calc123"));
    }
}