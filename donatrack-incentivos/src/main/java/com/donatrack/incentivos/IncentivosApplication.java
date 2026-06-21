package com.donatrack.incentivos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.donatrack")
public class IncentivosApplication {
  public static void main(String[] args) {
    SpringApplication.run(IncentivosApplication.class, args);
  }
}
