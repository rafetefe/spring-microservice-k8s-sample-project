package rafetefe.api.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import rafetefe.api.Entity.Order;

import java.util.List;

public interface OrderController {

//    @PostMapping("/order")
    Mono<Void> createOrder(Order newOrder);

    @GetMapping("/ongoing")
    Flux<Order> getOngoingOrders();

    @GetMapping("/complete")
    Flux<Order> getCompleteOrders();

    @GetMapping("/cancelled")
    Flux<Order> getCancelledOrders();

    //can be event driven. but is not going to be in used in this version of the project.
    @PostMapping("/cancel/{orderId}")
    Mono<Void> cancelOrder(@PathVariable int orderId);  

    @PostMapping("/complete/{orderId}")
    Mono<Void> completeOrder(@PathVariable int orderId);
}
