package com.uestc.studentagent.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@MapperScan("com.uestc.studentagent.backend")
@ConfigurationPropertiesScan
public class StudentAgentBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentAgentBackendApplication.class, args);
    }
}
