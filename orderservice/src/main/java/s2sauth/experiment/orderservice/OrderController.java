package s2sauth.experiment.orderservice;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @PostMapping
    public Map<String, Object> createOrder(@RequestBody(required = false) Map<String, Object> body) {
        return Map.of(
                "orderId", UUID.randomUUID().toString(),
                "status", "accepted"
        );
    }
}
