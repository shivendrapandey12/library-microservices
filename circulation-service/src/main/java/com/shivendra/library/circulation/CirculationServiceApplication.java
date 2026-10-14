package com.shivendra.library.circulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CirculationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CirculationServiceApplication.class, args);
    }
}
