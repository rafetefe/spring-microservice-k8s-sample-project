package rafetefe.composite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import rafetefe.api.Controller.ProductController;
import rafetefe.api.Entity.Product;
import rafetefe.api.Event;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.cloud.stream.function.StreamBridge;
import reactor.core.scheduler.Scheduler;

import java.util.logging.Level;

import static rafetefe.api.Event.Type.CREATE;

@Component
public class CompositeIntegration implements ProductController {

    private static final Logger LOG = LoggerFactory.getLogger(CompositeIntegration.class);

    private WebClient webClient;

    private String productHostname;
    private int productPort;

    private StreamBridge streamBridge;

    private Scheduler publishEventScheduler;

    //things to test.
    // due to our implementing the core service controllers
    // we have method for calling every core-service.
    // check if this necessary?
    // todo
    // after doing implementing all methods for communicating with cores
    // check if commenting some functions still allow for semi utilization of core services.

    /*
    * Notes while generating urls.
    * http:// is required at the begining. for numeric ips or domainnames (localhost)
    *
    * */

    @Autowired
    public CompositeIntegration(WebClient.Builder webClient,
                                @Value("${app.product.host}") String productHostname,
                                @Value("${app.product.port}") int productPort,
                                StreamBridge streamBridge,
                                @Qualifier("publishEventScheduler") Scheduler publishEventScheduler){
        this.webClient = webClient.build();
        this.streamBridge = streamBridge;
        this.publishEventScheduler = publishEventScheduler;

        this.productHostname = productHostname;
        this.productPort = productPort;
    }

    private void sendMessage(String bindingName, Event event) {
        LOG.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }

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
                .log(LOG.getName(), Level.FINE)
                .onErrorMap(ex->new Exception("compositeIntegration.getProduct error:"+ex.getMessage()));
    }

    @Override
    public Flux<Product> getAll() {
        return null;
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        return null;
    }

    @Override
    public Mono<String> webClientTest() {
        return null;
    }
    //this is like repository of core microservices. but
    //communicates with other services instead of a db.

}
