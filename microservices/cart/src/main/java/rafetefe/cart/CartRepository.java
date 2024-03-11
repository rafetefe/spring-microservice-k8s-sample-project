package rafetefe.cart;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import rafetefe.api.Entity.Cart;
import reactor.core.publisher.Mono;

@Repository
public interface CartRepository extends ReactiveCrudRepository<Cart, String> {
    Mono<Cart> findByOwnerId(int ownerId);
}
