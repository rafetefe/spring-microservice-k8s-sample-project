package rafetefe.order;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rafetefe.api.Entity.Order;
import rafetefe.api.Event;


@Configuration
public class MessageProcessorConfig {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

    private final OrderService orderService;

    @Autowired
    public MessageProcessorConfig(OrderService orderService) {
        this.orderService = orderService;
    }

    @Bean
    public Consumer<Event<Integer, Order>> messageProcessor() {
        return event -> {
            LOG.info("Process message created at {}...", event.getEventCreatedAt());

            switch (event.getEventType()) {

                case CREATE:
                    //createOrder
                    Order newOrder = event.getData();
                    Integer userId = event.getKey();
                    LOG.info("Recording a new Order:{}, for user:{}.", newOrder.getOrderId(), userId);
                    orderService.createOrder(newOrder).block();
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
