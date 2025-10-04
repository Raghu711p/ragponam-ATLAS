package com.studentevaluator.dto;

import java.time.LocalDateTime;

import com.studentevaluator.model.Assignment;

/**
 * DTO for assignment responses.
 */
public class AssignmentResponse {
    
    private String assignmentId;
    private String title;
    private String description;
    private boolean hasTestFile;
    private LocalDateTime createdAt;
    
    public AssignmentResponse() {}
    
    public AssignmentResponse(Assignment assignment) {
        this.assignmentId = assignment.getAssignmentId();
        this.title = assignment.getTitle();
        this.description = assignment.getDescription();
        this.hasTestFile = assignment.getTestFilePath() != null;
        this.createdAt = assignment.getCreatedAt();
    }
    
    public String getAssignmentId() {
        return assignmentId;
    }
    
    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isHasTestFile() {
        return hasTestFile;
    }
    
    public void setHasTestFile(boolean hasTestFile) {
        this.hasTestFile = hasTestFile;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}