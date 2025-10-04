package com.studentevaluator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Assignment entity representing a programming assignment in the evaluation system.
 * Contains assignment metadata and references to associated test files.
 */
@Entity
@Table(name = "assignments")
public class Assignment {
    
    @Id
    @Column(name = "assignment_id", length = 50)
    @NotBlank(message = "Assignment ID cannot be blank")
    @Size(max = 50, message = "Assignment ID cannot exceed 50 characters")
    private String assignmentId;
    
    @Column(name = "title", length = 200, nullable = false)
    @NotBlank(message = "Assignment title cannot be blank")
    @Size(max = 200, message = "Assignment title cannot exceed 200 characters")
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "test_file_path", length = 500)
    @Size(max = 500, message = "Test file path cannot exceed 500 characters")
    private String testFilePath;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Default constructor for JPA
    public Assignment() {
    }
    
    // Constructor with required fields
    public Assignment(String assignmentId, String title) {
        this.assignmentId = assignmentId;
        this.title = title;
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with all fields
    public Assignment(String assignmentId, String title, String description, String testFilePath) {
        this.assignmentId = assignmentId;
        this.title = title;
        this.description = description;
        this.testFilePath = testFilePath;
        this.createdAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
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
    
    public String getTestFilePath() {
        return testFilePath;
    }
    
    public void setTestFilePath(String testFilePath) {
        this.testFilePath = testFilePath;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assignment that = (Assignment) o;
        return Objects.equals(assignmentId, that.assignmentId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }
    
    @Override
    public String toString() {
        return "Assignment{" +
                "assignmentId='" + assignmentId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", testFilePath='" + testFilePath + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}