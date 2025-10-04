package com.studentevaluator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentevaluator.dto.ErrorResponse;
import com.studentevaluator.exception.AssignmentNotFoundException;
import com.studentevaluator.exception.FileUploadException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for GlobalExceptionHandler.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void handleAssignmentNotFoundException_ShouldReturnNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/assignment-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
        
        assertThat(error.getCode()).isEqualTo("ASSIGNMENT_NOT_FOUND");
        assertThat(error.getMessage()).contains("test-assignment");
        assertThat(error.getTimestamp()).isNotNull();
    }
    
    @Test
    void handleFileUploadException_ShouldReturnBadRequest() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/file-upload-error"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
        
        assertThat(error.getCode()).isEqualTo("FILE_UPLOAD_ERROR");
        assertThat(error.getMessage()).contains("Invalid file");
        assertThat(error.getTimestamp()).isNotNull();
    }
    
    @Test
    void handleMaxUploadSizeExceededException_ShouldReturnPayloadTooLarge() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/file-size-exceeded"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
        
        assertThat(error.getCode()).isEqualTo("FILE_SIZE_EXCEEDED");
        assertThat(error.getMessage()).contains("maximum allowed limit");
        assertThat(error.getTimestamp()).isNotNull();
    }
    
    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequest() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
        
        assertThat(error.getCode()).isEqualTo("INVALID_ARGUMENT");
        assertThat(error.getMessage()).contains("Invalid parameter");
        assertThat(error.getTimestamp()).isNotNull();
    }
    
    @Test
    void handleRuntimeException_ShouldReturnInternalServerError() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/runtime-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
        
        assertThat(error.getCode()).isEqualTo("RUNTIME_ERROR");
        assertThat(error.getMessage()).contains("error occurred while processing");
        assertThat(error.getTimestamp()).isNotNull();
    }
    
    @Test
    void handleGenericException_ShouldReturnInternalServerError() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
        
        assertThat(error.getCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(error.getMessage()).contains("unexpected error occurred");
        assertThat(error.getTimestamp()).isNotNull();
    }
    
    /**
     * Test controller to trigger various exceptions for testing the global exception handler.
     */
    @RestController
    @RequestMapping("/test")
    static class TestController {
        
        @GetMapping("/assignment-not-found")
        public void throwAssignmentNotFoundException() {
            throw new AssignmentNotFoundException("test-assignment");
        }
        
        @GetMapping("/file-upload-error")
        public void throwFileUploadException() {
            throw new FileUploadException("Invalid file format");
        }
        
        @GetMapping("/file-size-exceeded")
        public void throwMaxUploadSizeExceededException() {
            throw new MaxUploadSizeExceededException(1024L);
        }
        
        @GetMapping("/illegal-argument")
        public void throwIllegalArgumentException() {
            throw new IllegalArgumentException("Invalid parameter value");
        }
        
        @GetMapping("/runtime-error")
        public void throwRuntimeException() {
            throw new RuntimeException("Something went wrong");
        }
        
        @GetMapping("/generic-error")
        public void throwGenericException() throws Exception {
            throw new Exception("Generic error occurred");
        }
    }
}