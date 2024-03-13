package rafetefe.composite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import rafetefe.api.Controller.CartController;
import rafetefe.api.Controller.OrderController;
import rafetefe.api.Controller.ProductController;
import rafetefe.api.Entity.Cart;
import rafetefe.api.Entity.Order;
import rafetefe.api.Entity.Product;
import rafetefe.api.Event;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.cloud.stream.function.StreamBridge;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.logging.Level;

import static java.util.logging.Level.FINE;
import static rafetefe.api.Event.Type.CREATE;
import static rafetefe.api.Event.Type.DELETE;
import static reactor.core.publisher.Flux.empty;

@Component
public class CompositeIntegration implements ProductController, CartController, OrderController {

    private static final Logger LOG = LoggerFactory.getLogger(CompositeIntegration.class);

    private WebClient webClient;

    private String productHostname;
    private int productPort;
    private String cartHostname;
    private int cartPort;
    private String orderHostname;
    private int orderPort;

    private StreamBridge streamBridge;

    private Scheduler publishEventScheduler;

    private int userId = 2024;

    /*
    * Notes while generating urls.
    * http:// is required at the begining. for numeric ips or domainnames (localhost)
    *
    *
    * If event returns a output, use streamBridge.fromCallable
    * if returns void/none, use sB.fromRunnable
    * */

    @Autowired
    public CompositeIntegration(WebClient.Builder webClient,
                                @Value("${app.product.host}") String productHostname,
                                @Value("${app.product.port}") int productPort,
                                @Value("${app.cart.host}") String cartHostname,
                                @Value("${app.cart.port}") int cartPort,
                                @Value("${app.order.host}") String orderHostname,
                                @Value("${app.order.port}") int orderPort,
                                StreamBridge streamBridge,
                                @Qualifier("publishEventScheduler") Scheduler publishEventScheduler){
        this.webClient = webClient.build();
        this.streamBridge = streamBridge;
        this.publishEventScheduler = publishEventScheduler;

        this.productHostname = productHostname;
        this.productPort = productPort;
        this.orderHostname = orderHostname;
        this.orderPort = orderPort;
        this.cartHostname = cartHostname;
        this.cartPort = cartPort;
    }

    private void sendMessage(String bindingName, Event event) {
        LOG.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }

    //                  PRODUCT                     //
    @Override
    public Mono<Product> createProduct(Product body) {
        // send the request to core service.
        // core service can now be lighter due some of the
        // data validation processes can be done here.
        // or the composite service can be lighter?
        // this question is a challenging and requires architecture experience
        // because i have tried to solve it before.
        return Mono.fromCallable(() -> {
            sendMessage("products-out-0", new Event(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        //    http://product:8080/product/id
        //  http://localhost:7001/product/id
        String functionURL="http://"+productHostname+":"+productPort+"/product/"+productId;
        return webClient.get().uri(functionURL)
                .retrieve().bodyToMono(Product.class)
                .log(LOG.getName(), FINE)
                .onErrorMap(ex->new Exception("compositeIntegration.getProduct error:"+ex.getMessage()));
    }

    @Override
    public Flux<Product> getAllProducts() {
        String functionURL = "http://"+productHostname+":"+productPort+"/products";
        return webClient.get().uri(functionURL).retrieve().bodyToFlux(Product.class).log(LOG.getName(), FINE).onErrorResume(error -> empty());
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        return Mono.fromRunnable(() -> sendMessage("products-out-0", new Event(DELETE, userId, productId)))
                .subscribeOn(publishEventScheduler).then();
    }

    //                      CART                        //
    @Override
    public Mono<Void> removeFromCart(int productId) {
        return Mono.fromRunnable(() -> sendMessage("carts-out-0", new Event(DELETE, userId, productId)))
                .subscribeOn(publishEventScheduler).then();
    }

    @Override
    public Mono<Void> clearCart() {
        return Mono.fromRunnable(() -> sendMessage("carts-out-0", new Event(DELETE, userId, null)))
                .subscribeOn(publishEventScheduler).then();
    }

    @Override
    public Mono<Void> addToCart(int productId){
        return Mono.fromRunnable(() -> sendMessage("carts-out-0", new Event(CREATE, userId, productId)))
                .subscribeOn(publishEventScheduler).then();
    }

    @Override
    public Flux<Integer> getCartContent() {
        // return webClient.get().uri(url).retrieve().bodyToFlux(Recommendation.class).log(LOG.getName(), FINE).onErrorResume(error -> empty());
        String functionURL = "http://"+cartHostname+":"+cartPort+"/cart";
        return webClient.get().uri(functionURL).retrieve().bodyToFlux(Integer.class).log(LOG.getName(), FINE).onErrorResume(error -> empty());
    }

    //                  ORDER                   //

    @Override
    public Mono<Void> createOrder(Order newOrder){
        return Mono.fromRunnable(() -> sendMessage("orders-out-0", new Event(CREATE, userId, newOrder)))
            .subscribeOn(publishEventScheduler).then();
    }

    @Override
    public Flux<Order> getOngoingOrders() {
        String functionURL = "http://"+orderHostname+":"+orderPort+"/ongoing";
        return webClient.get().uri(functionURL).retrieve().bodyToFlux(Order.class).log(LOG.getName(), FINE).onErrorResume(error -> empty());
    }

    @Override
    public Flux<Order> getCompleteOrders() {
        String functionURL = "http://"+orderHostname+":"+orderPort+"/complete";
        return webClient.get().uri(functionURL).retrieve().bodyToFlux(Order.class).log(LOG.getName(), FINE).onErrorResume(error -> empty());
    }

    @Override
    public Flux<Order> getCancelledOrders() {
        String functionURL = "http://"+orderHostname+":"+orderPort+"/cancelled";
        return webClient.get().uri(functionURL).retrieve().bodyToFlux(Order.class).log(LOG.getName(), FINE).onErrorResume(error -> empty());
    }

    @Override
    public Mono<Void> cancelOrder(int orderId) {
        return null;
    }

    @Override
    public Mono<Void> completeOrder(int orderId) {
        return null;
    }
}
