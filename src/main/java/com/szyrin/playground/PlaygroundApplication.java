package com.szyrin.playground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PlaygroundApplication {

  public static void main(String[] args) {
    SpringApplication.run(PlaygroundApplication.class, args);
  }

}
