package rafetefe.api.Controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import rafetefe.api.Entity.Cart;
import rafetefe.api.Entity.Order;
import rafetefe.api.Entity.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartController {

//    @DeleteMapping("/cart/{productId}")
    Mono<Void> removeFromCart(@PathVariable int productId);

//    @DeleteMapping("/cart")
    Mono<Void> clearCart();

//    @PostMapping("/cart")
//    Mono<Order> submitCart();

//    @PostMapping("/cart/{productId}")
    Mono<Void> addToCart(@PathVariable int productId);

    @GetMapping("/cart")
    Flux<Integer> getCartContent();
}
