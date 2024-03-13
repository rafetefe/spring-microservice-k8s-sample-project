package rafetefe.api.Controller;

import org.springframework.web.bind.annotation.*;
import rafetefe.api.Entity.Order;
import rafetefe.api.Entity.Product;
import rafetefe.api.Entity.Status;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CompositeController {

    //Product
    @GetMapping(value = "/product/{productId}", produces = "application/json")
    Mono<Product> getProduct(@PathVariable int productId);
    //works but fails returns error when duplicate

    @PostMapping(value = "/product", produces = "application/json", consumes = "application/json")
    Mono<Product> createProduct(@RequestBody Product body);

    @DeleteMapping(value= "/product/{productId}")
    Mono<Void> deleteProduct(@PathVariable int productId);

    //Cart
    @GetMapping(value="/cart")
    Flux<Integer> getCart();

    //Should it return anything?
    @PostMapping(value = "/cart/{productId}")
    Mono<Void> addToCart(@PathVariable int productId);

    @DeleteMapping(value = "/cart/{productId}")
    Mono<Void> removeFromCart(@PathVariable int productId);

    @DeleteMapping(value = "/cart")
    Mono<Void> clearCart();

    @PostMapping(value = "/cart")
    Mono<Void> createOrder();

    //Order
    @GetMapping(value= "/order/{type}")
    Flux<Order> getOrders(@PathVariable Status type);

    //skip order type modification? markComplete & setCancelled ..

}
