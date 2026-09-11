package com.donatrack.incentivos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.donatrack.incentivos")
@EnableScheduling
public class IncentivosApplication {
  public static void main(String[] args) {
    SpringApplication.run(IncentivosApplication.class, args);
  }
}
