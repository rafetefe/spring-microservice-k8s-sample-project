package rafetefe.api.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


import java.util.Date;
import java.util.List;
import java.util.Objects;

@Document(collection="orders")
public class Order {

    /*DB Attributes*/
    @Id
    private String id;
    @Version
    private Integer version;
    /**/
    @Indexed(unique = true)
    private int orderId;
    private int ownerId;
    private List<Product> content;
    private Date dateOrderInitiated;
    private Date dateOrderCompleted;
    private Status status;

    public Order(List<Product> content, int ownerId){
        this.content = content;
        this.ownerId = ownerId;
        this.dateOrderInitiated = new Date();
        this.status = Status.ONGOING;
    }

    public void completeOrder(){
        this.dateOrderCompleted = new Date();
        this.status = Status.COMPLETE;
    }

    public void cancelOrder(){
        this.dateOrderCompleted = new Date();
        this.status = Status.CANCELLED;
    }


    public Date getDateOrderInitiated() {
        return dateOrderInitiated;
    }

    public void setDateOrderInitiated(Date dateOrderInitiated) {
        this.dateOrderInitiated = dateOrderInitiated;
    }

    public Date getDateOrderCompleted() {
        return dateOrderCompleted;
    }

    public void setDateOrderCompleted(Date dateOrderCompleted) {
        this.dateOrderCompleted = dateOrderCompleted;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Product> getContent() {
        return content;
    }

    public void setContent(List<Product> content) {
        this.content = content;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId == order.orderId && ownerId == order.ownerId && Objects.equals(id, order.id) && Objects.equals(version, order.version) && Objects.equals(content, order.content) && Objects.equals(dateOrderInitiated, order.dateOrderInitiated) && Objects.equals(dateOrderCompleted, order.dateOrderCompleted) && status == order.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, orderId, ownerId, content, dateOrderInitiated, dateOrderCompleted, status);
    }
}
