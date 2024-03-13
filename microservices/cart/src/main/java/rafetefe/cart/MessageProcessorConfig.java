package rafetefe.cart;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rafetefe.api.Event;


@Configuration
public class MessageProcessorConfig {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

    private final CartService cartService;

    @Autowired
    public MessageProcessorConfig(CartService cartService) {
        this.cartService = cartService;
    }

    @Bean
    public Consumer<Event<Integer, Integer>> messageProcessor() {
        return event -> {
            LOG.info("Process message created at {}...", event.getEventCreatedAt());

            switch (event.getEventType()) {

                case CREATE:
                    //addToCart event, (CREATE, userID, productID)
                    Integer productId = event.getData();
                    Integer userId = event.getKey();
                    LOG.info("Adding product with ID to user cart.: pID:{}, uID:{}", productId, userId);
                    cartService.addToCart(productId).block();
                    break;

                case DELETE:
                    //ClearCart and RemoveFromCart Events. If body is null, then it's a cart clear signal.
                    Integer eventBody = event.getData();
                    if(eventBody == null){
                        LOG.info("Clearing user cart.");
                        cartService.clearCart().block();
                    }else{
                        LOG.info("Removing product with ID from user cart. {}", eventBody);
                        cartService.removeFromCart(eventBody).block();
                    }
                    break;

                default:
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                    LOG.warn(errorMessage);
                    throw new RuntimeException(errorMessage);
            }

            LOG.info("Message processing done!");

        };
    }
}
