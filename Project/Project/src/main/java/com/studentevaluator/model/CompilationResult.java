package com.studentevaluator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Model class representing the result of Java code compilation.
 * Contains compilation status, output, errors, and compiled class path.
 */
public class CompilationResult {
    
    private boolean successful;
    private String output;
    private List<String> errors;
    private String compiledClassPath;
    
    // Default constructor
    public CompilationResult() {
        this.errors = new ArrayList<>();
    }
    
    // Constructor for successful compilation
    public CompilationResult(boolean successful, String output, String compiledClassPath) {
        this.successful = successful;
        this.output = output;
        this.compiledClassPath = compiledClassPath;
        this.errors = new ArrayList<>();
    }
    
    // Constructor for failed compilation
    public CompilationResult(boolean successful, String output, List<String> errors) {
        this.successful = successful;
        this.output = output;
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        this.compiledClassPath = null;
    }
    
    // Constructor with all fields
    public CompilationResult(boolean successful, String output, List<String> errors, String compiledClassPath) {
        this.successful = successful;
        this.output = output;
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        this.compiledClassPath = compiledClassPath;
    }
    
    // Getters and Setters
    public boolean isSuccessful() {
        return successful;
    }
    
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
    
    public String getOutput() {
        return output;
    }
    
    public void setOutput(String output) {
        this.output = output;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
    }
    
    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }
    
    public String getCompiledClassPath() {
        return compiledClassPath;
    }
    
    public void setCompiledClassPath(String compiledClassPath) {
        this.compiledClassPath = compiledClassPath;
    }
    
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompilationResult that = (CompilationResult) o;
        return successful == that.successful &&
                Objects.equals(output, that.output) &&
                Objects.equals(errors, that.errors) &&
                Objects.equals(compiledClassPath, that.compiledClassPath);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(successful, output, errors, compiledClassPath);
    }
    
    @Override
    public String toString() {
        return "CompilationResult{" +
                "successful=" + successful +
                ", output='" + output + '\'' +
                ", errors=" + errors +
                ", compiledClassPath='" + compiledClassPath + '\'' +
                '}';
    }
}