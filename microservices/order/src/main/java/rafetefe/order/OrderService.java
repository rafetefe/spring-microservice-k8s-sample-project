package rafetefe.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import rafetefe.api.Controller.OrderController;
import rafetefe.api.Entity.Order;
import rafetefe.api.Entity.Status;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.logging.Level.FINE;

@RestController
public class OrderService implements OrderController {

    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final int userId = 2024;

    @Autowired
    public OrderService(OrderRepository orderRepository){
        this.orderRepository = orderRepository;
    }

    @Override
    public Mono<Void> createOrder(Order newOrder){
        return this.orderRepository.save(newOrder).log(LOG.getName(), FINE)
                .onErrorMap(ex->new RuntimeException("createOrder error:"+ex.getMessage()))
                .then();
    }

    @Override
    public Flux<Order> getOngoingOrders() {
        int ownerId = this.userId;
        return this.orderRepository.findAllByOwnerIdAndStatus(ownerId, Status.ONGOING)
                .log(LOG.getName(), FINE)
                .onErrorMap(ex-> new Exception("getOngoingOrders error:"+ex.getMessage()));
    }

    @Override
    public Flux<Order> getCompleteOrders() {
        int ownerId = this.userId;
        return this.orderRepository.findAllByOwnerIdAndStatus(ownerId, Status.COMPLETE)
                .log(LOG.getName(), FINE)
                .onErrorMap(ex-> new Exception("getCompleteOrders error:"+ex.getMessage()));
    }

    @Override
    public Flux<Order> getCancelledOrders() {
        int ownerId = this.userId;
        return this.orderRepository.findAllByOwnerIdAndStatus(ownerId, Status.CANCELLED)
                .log(LOG.getName(), FINE)
                .onErrorMap(ex-> new Exception("getCancelledOrders error:"+ex.getMessage()));
    }


    @Override
    public Mono<Void> cancelOrder(int orderId) {
        int ownerId = this.userId;

        return orderRepository.findByOrderId(orderId)
                .log(LOG.getName(), FINE)
                .onErrorMap(ex -> new Exception("cancelOrder error:"+ ex.getMessage()))
                .map(foundOrder ->  {
                    foundOrder.cancelOrder();
                    return orderRepository.save(foundOrder);
                }).flatMap(e->e).then();

    }

    @Override
    public Mono<Void> completeOrder(int orderId) {
        int ownerId = this.userId;

        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new Exception
                        ("completeOrder error: findByOrderId returned nothing for given orderId, "+orderId)
                ))
                .log(LOG.getName(), FINE)
                .onErrorMap(ex -> new Exception("completeOrder error:"+ex.getMessage()))
                .map(foundOrder -> {
                    foundOrder.completeOrder();
                    return orderRepository.save(foundOrder);
                }).flatMap(e->e).then();

    }



}
