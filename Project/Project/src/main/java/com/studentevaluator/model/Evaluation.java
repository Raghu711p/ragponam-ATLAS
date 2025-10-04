package com.studentevaluator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evaluation entity representing an evaluation of a student's assignment submission.
 * Contains foreign key relationships to Student and Assignment entities.
 */
@Entity
@Table(name = "evaluations")
public class Evaluation {
    
    @Id
    @Column(name = "evaluation_id", length = 50)
    @NotBlank(message = "Evaluation ID cannot be blank")
    @Size(max = 50, message = "Evaluation ID cannot exceed 50 characters")
    private String evaluationId;
    
    @Column(name = "student_id", length = 50, nullable = false)
    @NotBlank(message = "Student ID cannot be blank")
    private String studentId;
    
    @Column(name = "assignment_id", length = 50, nullable = false)
    @NotBlank(message = "Assignment ID cannot be blank")
    private String assignmentId;
    
    @Column(name = "score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Score cannot be negative")
    @DecimalMax(value = "999.99", message = "Score cannot exceed 999.99")
    private BigDecimal score;
    
    @Column(name = "max_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Max score cannot be negative")
    @DecimalMax(value = "999.99", message = "Max score cannot exceed 999.99")
    private BigDecimal maxScore;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Evaluation status cannot be null")
    private EvaluationStatus status;
    
    @Column(name = "evaluated_at", nullable = false)
    private LocalDateTime evaluatedAt;
    
    // Foreign key relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", insertable = false, updatable = false)
    private Student student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", referencedColumnName = "assignment_id", insertable = false, updatable = false)
    private Assignment assignment;
    
    // Default constructor for JPA
    public Evaluation() {
    }
    
    // Constructor with required fields
    public Evaluation(String evaluationId, String studentId, String assignmentId, EvaluationStatus status) {
        this.evaluationId = evaluationId;
        this.studentId = studentId;
        this.assignmentId = assignmentId;
        this.status = status;
        this.evaluatedAt = LocalDateTime.now();
    }
    
    // Constructor with all fields
    public Evaluation(String evaluationId, String studentId, String assignmentId, 
                     BigDecimal score, BigDecimal maxScore, EvaluationStatus status) {
        this.evaluationId = evaluationId;
        this.studentId = studentId;
        this.assignmentId = assignmentId;
        this.score = score;
        this.maxScore = maxScore;
        this.status = status;
        this.evaluatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (evaluatedAt == null) {
            evaluatedAt = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public String getEvaluationId() {
        return evaluationId;
    }
    
    public void setEvaluationId(String evaluationId) {
        this.evaluationId = evaluationId;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getAssignmentId() {
        return assignmentId;
    }
    
    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }
    
    public BigDecimal getScore() {
        return score;
    }
    
    public void setScore(BigDecimal score) {
        this.score = score;
    }
    
    public BigDecimal getMaxScore() {
        return maxScore;
    }
    
    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }
    
    public EvaluationStatus getStatus() {
        return status;
    }
    
    public void setStatus(EvaluationStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }
    
    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public Assignment getAssignment() {
        return assignment;
    }
    
    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evaluation that = (Evaluation) o;
        return Objects.equals(evaluationId, that.evaluationId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(evaluationId);
    }
    
    @Override
    public String toString() {
        return "Evaluation{" +
                "evaluationId='" + evaluationId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", assignmentId='" + assignmentId + '\'' +
                ", score=" + score +
                ", maxScore=" + maxScore +
                ", status=" + status +
                ", evaluatedAt=" + evaluatedAt +
                '}';
    }
}