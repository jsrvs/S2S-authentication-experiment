package s2sauth.experiment.ordercreationservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient menuClient(SslBundles sslBundles,
                                 @Value("${downstream.menu.url}") String menuUrl) {
        return buildMtlsClient(sslBundles, menuUrl);
    }

    @Bean
    public RestClient orderClient(SslBundles sslBundles,
                                  @Value("${downstream.order.url}") String orderUrl) {
        return buildMtlsClient(sslBundles, orderUrl);
    }

    private RestClient buildMtlsClient(SslBundles sslBundles, String baseUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .sslContext(sslBundles.getBundle("client").createSslContext())
                .build();
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }
}
