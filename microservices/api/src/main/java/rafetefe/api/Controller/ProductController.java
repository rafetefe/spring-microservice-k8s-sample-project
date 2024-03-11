package rafetefe.api.Controller;

import org.springframework.web.bind.annotation.*;
import rafetefe.api.Entity.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


public interface ProductController {

//    @PostMapping(value = "/product", produces = "application/json", consumes = "application/json")
    Mono<Product> createProduct(Product body);

    @GetMapping(value = "/product/{productId}", produces = "application/json")
    Mono<Product> getProduct(@PathVariable int productId);

    @GetMapping("/products")
    Flux<Product> getAll();

//    @DeleteMapping(value = "/product/{productId}")
    Mono<Void> deleteProduct(int productId);

    //function for testing Webclient
    @GetMapping("/webClientTest")
    Mono<String> webClientTest();
}
