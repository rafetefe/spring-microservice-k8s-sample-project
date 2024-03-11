package rafetefe.composite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import rafetefe.api.Controller.CompositeController;
import rafetefe.api.Entity.Product;
import reactor.core.publisher.Mono;

@RestController
public class CompositeService implements CompositeController {

    private static final Logger LOG = LoggerFactory.getLogger(CompositeService.class);

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
}
