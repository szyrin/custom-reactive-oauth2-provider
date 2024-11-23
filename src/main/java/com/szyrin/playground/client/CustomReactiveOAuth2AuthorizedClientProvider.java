package com.szyrin.playground.client;

import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.ReactiveOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;

public final class CustomReactiveOAuth2AuthorizedClientProvider implements ReactiveOAuth2AuthorizedClientProvider {

  private ReactiveOAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient = new WebClientReactiveAuthorizationCodeTokenResponseClient();

  private Duration clockSkew = Duration.ofSeconds(60);

  private Clock clock = Clock.systemUTC();

  public static final String LOGIN_URI_ATTRIBUTE_NAME = OAuth2AuthorizationContext.class.getName().concat(".LOGIN_URI");

  @Override
  public Mono<OAuth2AuthorizedClient> authorize(OAuth2AuthorizationContext context) {
    Assert.notNull(context, "context cannot be null");
    ClientRegistration clientRegistration = context.getClientRegistration();
    if (!AuthorizationGrantType.AUTHORIZATION_CODE.equals(clientRegistration.getAuthorizationGrantType())) {
      return Mono.empty();
    }
    OAuth2AuthorizedClient authorizedClient = context.getAuthorizedClient();
    if (authorizedClient != null && !hasTokenExpired(authorizedClient.getAccessToken())) {
      // If client is already authorized but access token is NOT expired than no
      // need for re-authorization
      return Mono.empty();
    }

    String authorizationUri = clientRegistration.getProviderDetails().getAuthorizationUri();
    String clientId = clientRegistration.getClientId();
    String redirectUri = clientRegistration.getRedirectUri();

    String code = getCode(context, authorizationUri, clientId, redirectUri);

    OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse.success(code).redirectUri(redirectUri).build();

    OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
        .attributes((attrs) ->
            attrs.put(OAuth2ParameterNames.REGISTRATION_ID, clientRegistration.getRegistrationId()))
        .clientId(clientRegistration.getClientId())
        .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
        .redirectUri(redirectUri)
        .build();
    OAuth2AuthorizationCodeGrantRequest authenticationRequest = new OAuth2AuthorizationCodeGrantRequest(clientRegistration,
        new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse));

    return Mono.just(authenticationRequest)
        .flatMap(this.accessTokenResponseClient::getTokenResponse)
        .onErrorMap(OAuth2AuthorizationException.class,
            (ex) -> new ClientAuthorizationException(ex.getError(), clientRegistration.getRegistrationId(), ex))
        .map((tokenResponse) -> new OAuth2AuthorizedClient(clientRegistration, context.getPrincipal().getName(),
            tokenResponse.getAccessToken(), tokenResponse.getRefreshToken()));
  }

  private static String getCode(OAuth2AuthorizationContext context, String authorizationUri, String clientId, String redirectUri) {
    WebClient webClient = WebClient.builder().build();
    String username = context.getAttribute(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME);
    String password = context.getAttribute(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME);
    String loginUri = context.getAttribute(LOGIN_URI_ATTRIBUTE_NAME);
    // here use webclient to get the authorization code
    String code = "sample_code";
    return code;
  }

  private boolean hasTokenExpired(OAuth2Token token) {
    return this.clock.instant().isAfter(token.getExpiresAt().minus(this.clockSkew));
  }

  public void setAccessTokenResponseClient(
      ReactiveOAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient) {
    Assert.notNull(accessTokenResponseClient, "accessTokenResponseClient cannot be null");
    this.accessTokenResponseClient = accessTokenResponseClient;
  }

  public void setClockSkew(Duration clockSkew) {
    Assert.notNull(clockSkew, "clockSkew cannot be null");
    Assert.isTrue(clockSkew.getSeconds() >= 0, "clockSkew must be >= 0");
    this.clockSkew = clockSkew;
  }

  public void setClock(Clock clock) {
    Assert.notNull(clock, "clock cannot be null");
    this.clock = clock;
  }
}
