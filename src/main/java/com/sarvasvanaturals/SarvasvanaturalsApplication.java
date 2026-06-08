package com.sarvasvanaturals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SarvasvanaturalsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SarvasvanaturalsApplication.class, args);
    }
}
