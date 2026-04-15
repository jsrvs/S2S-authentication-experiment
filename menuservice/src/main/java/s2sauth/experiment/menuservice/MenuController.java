package s2sauth.experiment.menuservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/items")
public class MenuController {

    @GetMapping("/{id}")
    public Map<String, Object> getItem(@PathVariable String id) {
        return Map.of(
                "id", id,
                "name", "Cheeseburger",
                "price", 5.99
        );
    }
}
