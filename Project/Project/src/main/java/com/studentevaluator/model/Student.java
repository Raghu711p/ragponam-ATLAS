package com.studentevaluator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Student entity representing a student in the evaluation system.
 * Stores student information and is referenced by evaluations.
 */
@Entity
@Table(name = "students")
public class Student {
    
    @Id
    @Column(name = "student_id", length = 50)
    @NotBlank(message = "Student ID cannot be blank")
    @Size(max = 50, message = "Student ID cannot exceed 50 characters")
    private String studentId;
    
    @Column(name = "name", length = 100, nullable = false)
    @NotBlank(message = "Student name cannot be blank")
    @Size(max = 100, message = "Student name cannot exceed 100 characters")
    private String name;
    
    @Column(name = "email", length = 100)
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Default constructor for JPA
    public Student() {
    }
    
    // Constructor with required fields
    public Student(String studentId, String name) {
        this.studentId = studentId;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with all fields
    public Student(String studentId, String name, String email) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
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
        Student student = (Student) o;
        return Objects.equals(studentId, student.studentId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }
    
    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}