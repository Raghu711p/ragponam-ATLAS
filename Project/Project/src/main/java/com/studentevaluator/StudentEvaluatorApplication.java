package com.studentevaluator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class StudentEvaluatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentEvaluatorApplication.class, args);
    }
}