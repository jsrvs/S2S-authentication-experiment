package s2sauth.experiment.ordercreationservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderCreationController {

    private final RestClient menuClient;
    private final RestClient orderClient;

    public OrderCreationController(
            @Value("${downstream.menu.url}") String menuUrl,
            @Value("${downstream.order.url}") String orderUrl,
            @Value("${s2s.api-key}") String apiKey) {
        this.menuClient = RestClient.builder()
                .baseUrl(menuUrl)
                .defaultHeader("X-API-Key", apiKey)
                .build();
        this.orderClient = RestClient.builder()
                .baseUrl(orderUrl)
                .defaultHeader("X-API-Key", apiKey)
                .build();
    }

    @PostMapping
    public Map<String, Object> createOrder(@RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> menuItem = menuClient.get()
                .uri("/items/1")
                .retrieve()
                .body(Map.class);

        Map<String, Object> order = orderClient.post()
                .uri("/orders")
                .body(Map.of("itemId", menuItem != null ? menuItem.get("id") : "1"))
                .retrieve()
                .body(Map.class);

        return Map.of(
                "menuItem", menuItem,
                "order", order
        );
    }
}
