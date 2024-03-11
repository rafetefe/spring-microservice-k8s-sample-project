package rafetefe.cart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import rafetefe.api.Controller.CartController;
import rafetefe.api.Entity.Cart;
import rafetefe.api.Entity.Product;
import rafetefe.cart.CartRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.logging.Level.FINE;

@RestController
public class CartService implements CartController {

    private static final Logger LOG = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;

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
    public Mono<Cart> removeFromCart(int productId) {
        int ownerId = this.userId;
        return cartRepository.findByOwnerId(ownerId)
                .switchIfEmpty(Mono.error(new Exception("Cart failed to  found:"+productId)))
                .log(LOG.getName(),FINE)
                .map(foundCart -> {
                    foundCart.removeByProductId(productId);
                    return cartRepository.save(foundCart);
                    }).flatMap(e->e);
    }

    @Override
    public Mono<Cart> clearCart() {
        int ownerId = this.userId;
        return cartRepository.findByOwnerId(ownerId)
                .switchIfEmpty(this.initUserCart())
                .log(LOG.getName(), FINE)
                .map(foundCart -> {
                    foundCart.clearContentList();
                    return cartRepository.save(foundCart);
                }).flatMap(e->e);


    }

    //TODO Composite service function
//    @Override
//    public Mono<Order> submitCart() {
//        int ownerId = this.userId;
//
//        return cartRepository.findByOwnerId(ownerId)
//                .onErrorMap(ex-> new Exception("submitCart error:"+ex.getMessage()))
//                .filter(cart -> !cart.getContent().isEmpty() )
//                .map(foundCart -> {
//                    Order newOrder = new Order(foundCart.getContent(), foundCart.getOwnerId());
//                    foundCart.clearContentList();
//                    return Mono.zip(cartRepository.save(foundCart),orderRepository.save(newOrder))
//                            .map(objects -> objects.getT2());
//                }).flatMap(e->e);
//
//    }


    //TODO COMPOSITE SERVICE FUNCTION
//    @Override
//    public Mono<Cart> addToCart(int productId) {
//        int ownerId = this.userId;
//
//        Mono<Product> foundProduct = productRepository.findByProductId(productId)
//                .switchIfEmpty(Mono.error(new Exception("productRepo, requested product not found.")))
//                .log(LOG.getName(), FINE)
//                .onErrorMap(ex->new Exception("productRepo, product query returned error:"+ ex.getMessage()));
//
//        Mono<Cart> foundCart = cartRepository.findByOwnerId(ownerId)
//                .switchIfEmpty(this.initUserCart())
//                .log(LOG.getName(), FINE)
//                .onErrorMap(ex->new Exception("cartRepo, cart query returned error:"+ ex.getMessage()));
//
//        return foundProduct.map(p->
//                foundCart.map(
//                        cart -> {
//                            cart.addProduct(p);
//                            return cartRepository.save(cart);
//                        }).flatMap(e->e)
//        ).flatMap(e->e);
//
//
//    }

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
