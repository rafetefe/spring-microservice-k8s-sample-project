package rafetefe.cart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import rafetefe.api.Controller.CartController;
import rafetefe.api.Entity.Cart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.logging.Level.FINE;

@RestController
public class CartService implements CartController {

    private static final Logger LOG = LoggerFactory.getLogger(CartService.class);

    CartRepository cartRepository;

    private int userId = 2024;

    public CartService(CartRepository cartRepository){
        this.cartRepository = cartRepository;
    }

    /*
        RULES:
        Cart contains product ids. not product objects. Orders will keep the product objects.
     */

    private Mono<Cart> initUserCart(){
        int ownerId = this.userId;
        Cart cart = new Cart(ownerId);
        return cartRepository.save(cart);
    }

    @Override
    public Mono<Void> removeFromCart(int productId) {
        int ownerId = this.userId;
        return cartRepository.findByOwnerId(ownerId)
                .switchIfEmpty(Mono.error(new Exception("Cart failed to  found:"+productId)))
                .log(LOG.getName(),FINE)
                .map(foundCart -> {
                    foundCart.removeByProductId(productId);
                    return cartRepository.save(foundCart);
                }).flatMap(e->e).then();
    }

    @Override
    public Mono<Void> clearCart() {
        int ownerId = this.userId;
        return cartRepository.findByOwnerId(ownerId)
                .switchIfEmpty(this.initUserCart())
                .log(LOG.getName(), FINE)
                .map(foundCart -> {
                    foundCart.clearContentList();
                    return cartRepository.save(foundCart);
                }).flatMap(e->e).then();


    }

    @Override
    public Mono<Void> addToCart(int productId) {
        int ownerId = this.userId;

        Mono<Cart> foundCart = cartRepository.findByOwnerId(ownerId)
                .switchIfEmpty(this.initUserCart())
                .log(LOG.getName(), FINE)
                .onErrorMap(ex->new Exception("cartRepo, cart query returned error:"+ ex.getMessage()));

        return foundCart.map(cart -> {
            cart.addProduct(productId);
            return cartRepository.save(cart);
            }).flatMap(c->c).then();
    }

    @Override
    public Flux<Integer> getCartContent() {
        int ownerId = this.userId;
        return cartRepository.findByOwnerId(ownerId)
                .switchIfEmpty(this.initUserCart())
                .log(LOG.getName(), FINE)
                .flatMapMany(cart -> {
                    return Flux.fromIterable(cart.getContent());
                });
    }
}
