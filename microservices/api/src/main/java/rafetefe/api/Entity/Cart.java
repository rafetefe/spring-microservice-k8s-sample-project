package rafetefe.api.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.ArrayList;

@Document(collection="carts")
public class Cart {

    /*DB Attributes*/
    @Id
    private String id;
    @Version
    private Integer version;
    /**/

    //ID of the Owner
    @Indexed(unique = true)
    private int ownerId;

    private List<Integer> content;

    public Cart(int ownerId){
        this.content = new ArrayList<Integer>();
        this.ownerId = ownerId;
    }

    public void clearContentList(){
        this.content = new ArrayList<Integer>();
    }

    public List<Integer> getContent() {
        return content;
    }

    public void addProduct(Integer productId) {
        this.content.add(productId);
    }

    public void removeByProductId(Integer idToRemove){
        //can be made faster by using cartElementNo rather productID
        //but no need to optimize atm
        for (int i = 0; i < this.content.size(); i++) {
            if(this.content.get(i).equals(idToRemove)){
                this.content.remove(i);
                return;
            }
        }
        //content.removeIf(x->x.getId().equals(removedId));

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

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
}
