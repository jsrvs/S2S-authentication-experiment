package s2sauth.experiment.ordercreationservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Configuration
public class RestClientConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);
        manager.setAuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build());
        return manager;
    }

    @Bean("menuClient")
    public RestClient menuClient(@Value("${downstream.menu.url}") String menuUrl,
                                 OAuth2AuthorizedClientManager clientManager) {
        return RestClient.builder()
                .baseUrl(menuUrl)
                .requestInterceptor(oauth2Interceptor(clientManager))
                .build();
    }

    @Bean("orderClient")
    public RestClient orderClient(@Value("${downstream.order.url}") String orderUrl,
                                  OAuth2AuthorizedClientManager clientManager) {
        return RestClient.builder()
                .baseUrl(orderUrl)
                .requestInterceptor(oauth2Interceptor(clientManager))
                .build();
    }

    private ClientHttpRequestInterceptor oauth2Interceptor(OAuth2AuthorizedClientManager clientManager) {
        return (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId("ordercreation-client")
                    .principal("ordercreation-service")
                    .build();
            OAuth2AuthorizedClient client = clientManager.authorize(authorizeRequest);
            request.getHeaders().setBearerAuth(client.getAccessToken().getTokenValue());
            return execution.execute(request, body);
        };
    }
}
