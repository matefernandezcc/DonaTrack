package com.donatrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.donatrack")
public class Donatrack {
  public static void main(String[] args) {
    SpringApplication.run(Donatrack.class, args);
  }
}
