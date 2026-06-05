package stockService;

import jakarta.persistence.*;

@Entity
@Table(name = "stock")
public class StockModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String productCode;

    @Column(nullable = false)
    private int quantity;

    public StockModel() {
    }

    public StockModel(String productCode, int quantity) {
        this.productCode = productCode;
        this.quantity = quantity;
    }

    public StockModel(int id, String productCode, int quantity) {
        this.id = id;
        this.productCode = productCode;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public StockModel setId(int id) {
        this.id = id;
        return this;
    }

    public String getProductCode() {
        return productCode;
    }

    public StockModel setProductCode(String productCode) {
        this.productCode = productCode;
        return this;
    }

    public int getQuantity() {
        return quantity;
    }

    public StockModel setQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }
}