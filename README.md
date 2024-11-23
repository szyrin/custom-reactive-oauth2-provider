# custom-reactive-oauth2-provider

This repository demonstrates the usage of a custom Oauth2AuthorizedClientProvider
for the generated OpenAPI client, written in the reactive style.

When the authorization code is not accessible via standard means (through
a web application that redirects to an authorization form), a custom authorized 
client provider can be used to retrieve the authorization code and exchange it for
the access token.

Another issue is when an OpenAPI generator is used for the Feign client generation:
`spring` generator with `spring-cloud` library does not have a template that would
support `ReactiveOAuth2AuthorizedClientManager`, since the `reactive` flag at the moment
is supported for `spring-boot` library only.

See documentation https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/spring.md
and the template https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator/src/main/resources/JavaSpring/libraries/spring-cloud/clientConfiguration.mustache

This repository contains a coarsely modified template which gives you the idea of
how the reactive support could be added.
