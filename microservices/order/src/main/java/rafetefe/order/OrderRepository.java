package rafetefe.order;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import rafetefe.api.Entity.Order;
import rafetefe.api.Entity.Status;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, String> {
    Mono<Order> findByOrderId(int orderId);
    Flux<Order> findAllByOwnerId(int ownerId);
    Flux<Order> findAllByOwnerIdAndStatus(int ownerId, Status status);
}
