package com.donatrack.donaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.donatrack")
public class DonacionesApplication {
  public static void main(String[] args) {
    SpringApplication.run(DonacionesApplication.class, args);
  }
}
