package s2sauth.experiment.gatewayservice;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;

@Component
public class OAuth2ClientCredentialsGatewayFilterFactory
        extends AbstractGatewayFilterFactory<Object> {

    private final ReactiveOAuth2AuthorizedClientManager clientManager;

    public OAuth2ClientCredentialsGatewayFilterFactory(
            ReactiveOAuth2AuthorizedClientManager clientManager) {
        super(Object.class);
        this.clientManager = clientManager;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId("gateway-client")
                    .principal("gateway-service")
                    .build();

            return clientManager.authorize(authorizeRequest)
                    .flatMap(authorizedClient -> {
                        String token = authorizedClient.getAccessToken().getTokenValue();
                        ServerHttpRequest decorated = new ServerHttpRequestDecorator(exchange.getRequest()) {
                            @Override
                            public HttpHeaders getHeaders() {
                                HttpHeaders headers = new HttpHeaders();
                                headers.putAll(super.getHeaders());
                                headers.setBearerAuth(token);
                                return headers;
                            }
                        };
                        return chain.filter(exchange.mutate().request(decorated).build());
                    });
        };
    }
}
