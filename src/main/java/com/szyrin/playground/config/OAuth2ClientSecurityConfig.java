package com.szyrin.playground.config;

import com.szyrin.playground.client.CustomReactiveOAuth2AuthorizedClientProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class OAuth2ClientSecurityConfig {

  @Value("${test.login-uri}")
  private String loginUri;
  @Value("${test.client-logname}")
  private String clientLogname;
  @Value("${test.client-password}")
  private String clientPassword;

  @Bean
  WebClient webClient(ReactiveOAuth2AuthorizedClientManager reactiveOAuth2AuthorizedClientManager) {
    ServerOAuth2AuthorizedClientExchangeFilterFunction oauth
        = new ServerOAuth2AuthorizedClientExchangeFilterFunction(reactiveOAuth2AuthorizedClientManager);
    oauth.setDefaultClientRegistrationId("test-login");
    return WebClient.builder()
        .filter(oauth)
        .build();
  }

  @Bean
  public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
      ReactiveClientRegistrationRepository clientRegistrationRepository,
      ReactiveOAuth2AuthorizedClientService authorizedClientService) {
    AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientServiceReactiveOAuth2AuthorizedClientManager
        = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
    ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
        .provider(new CustomReactiveOAuth2AuthorizedClientProvider())
        .refreshToken()
//                Force token refresh (default is 1 minute before expiry)
//                .refreshToken(t -> t.clockSkew(Duration.ofHours(3)))
        .build();
    authorizedClientServiceReactiveOAuth2AuthorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
    authorizedClientServiceReactiveOAuth2AuthorizedClientManager.setContextAttributesMapper(
        oAuth2AuthorizeRequest -> Mono.just(Map.of(
            OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, clientLogname,
            OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, clientPassword,
            // providing custom parameters into OAuth2AuthorizationContext is possible, example:
            CustomReactiveOAuth2AuthorizedClientProvider.LOGIN_URI_ATTRIBUTE_NAME, loginUri)));
    return authorizedClientServiceReactiveOAuth2AuthorizedClientManager;
  }
}
