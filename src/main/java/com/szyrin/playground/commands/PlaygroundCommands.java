package com.szyrin.playground.commands;

import com.szyrin.playground.client.V0ApiClient;
import com.szyrin.playground.client.model.GetSample200Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent("API generated client playground")
public class PlaygroundCommands {

  @Autowired
  private V0ApiClient v0ApiClient;

  @ShellMethod("API call")
  public String getSample() {
    ResponseEntity<GetSample200Response> response = v0ApiClient.getSample();
    return response.getBody().getSample();
  }
}
