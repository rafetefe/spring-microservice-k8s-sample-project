package rafetefe.composite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import rafetefe.api.Controller.CompositeController;
import rafetefe.api.Entity.Order;
import rafetefe.api.Entity.Product;
import rafetefe.api.Entity.Status;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.logging.Level.FINE;

@RestController
public class CompositeService implements CompositeController {

    private static final Logger LOG = LoggerFactory.getLogger(CompositeService.class);

    private int userId = 2024;

    private CompositeIntegration compositeIntegration;

    @Autowired
    public CompositeService(CompositeIntegration compositeIntegration){
        this.compositeIntegration = compositeIntegration;
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        if(0>productId){
            return null;
        }
        return compositeIntegration.getProduct(productId);
    }

    @Override
    public Mono<Product> createProduct(Product body) {
        try{
            //small validation of body before sending through kafka
            Product product = new Product(body.getProductId(), body.getName(), body.getPrice());
            return compositeIntegration.createProduct(product);
        } catch (RuntimeException re) {
            LOG.warn("createCompositeProduct failed: {}", re.toString());
            throw re;
        }
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        if(0>productId){
            return null;
        }
        return compositeIntegration.deleteProduct(productId);
    }

    @Override
    public Flux<Integer> getCart() {
        return compositeIntegration.getCartContent();
    }

    @Override
    public Mono<Void> addToCart(int productId) {
        if(0>productId){
            return null;
        }
        return compositeIntegration.addToCart(productId);
    }

    @Override
    public Mono<Void> removeFromCart(int productId) {
        if(0>productId){
            return null;
        }
        return compositeIntegration.removeFromCart(productId);
    }

    @Override
    public Mono<Void> clearCart() {
        return compositeIntegration.clearCart();
    }

    @Override
    public Mono<Void> createOrder() {
        //TODO Optimize orderCreation.
        // | Move the process to Order Service, since it's has the least power requirement
        // | Create a read pipe from Order to Product service so Order can keep the product details ?
        // |/ Should order keep the product details? wouldn't that make it duplicate?
        // |/ A pointer to a version of a product should be used instead. <Versioning system for products>
        // |/ So canceling the relocation.

        Mono<Void> orderCreation = compositeIntegration.getCartContent()
                .collectList()
                .map(list -> {
                    Order newOrder = new Order(list, userId);
                    return compositeIntegration.createOrder(newOrder);
                })
                .flatMap(e -> e).then();

        Mono<Void> cartClear = compositeIntegration.clearCart();

        return Mono.zip(f -> "", orderCreation, cartClear).then();
    }
    @Override
    public Flux<Order> getOrders(Status type) {
        switch(type){
            case ONGOING -> {
                return compositeIntegration.getOngoingOrders();
            }case CANCELLED -> {
                return compositeIntegration.getCancelledOrders();
            }case COMPLETE -> {
                return compositeIntegration.getCompleteOrders();
            }
            default -> {
                return null;
            }
        }
    }
}
