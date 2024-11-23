package com.szyrin.playground.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.web.reactive.function.client.WebClient;

@ShellComponent("OAuth2 API playground")
public class OAuthCommands {

  @Autowired
  private WebClient client;

  @ShellMethod("Sample call to API using WebClient")
  public String webclientSample() {
    String r = client.get().uri("http://localhost:8080/api/v0/sample")
        .retrieve().bodyToMono(String.class).block();
    return r;
  }
}
