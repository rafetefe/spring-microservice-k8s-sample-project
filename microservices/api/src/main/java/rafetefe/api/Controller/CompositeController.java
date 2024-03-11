package rafetefe.api.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import rafetefe.api.Entity.Product;
import reactor.core.publisher.Mono;

public interface CompositeController {
    @GetMapping(value = "/product/{productId}", produces = "application/json")
    Mono<Product> getProduct(@PathVariable int productId);

    @PostMapping(value = "/product", produces = "application/json", consumes = "application/json")
    Mono<Product> createProduct(@RequestBody Product body);
}
