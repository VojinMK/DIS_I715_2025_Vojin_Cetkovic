package productService;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class ProductModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String productCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private double price;

    public ProductModel() {
    }

    public ProductModel(String productCode, String name, String brand, String category, double price) {
        this.productCode = productCode;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.price = price;
    }

    public ProductModel(int id, String productCode, String name, String brand, String category, double price) {
        this.id = id;
        this.productCode = productCode;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public ProductModel setId(int id) {
        this.id = id;
        return this;
    }

    public String getProductCode() {
        return productCode;
    }

    public ProductModel setProductCode(String productCode) {
        this.productCode = productCode;
        return this;
    }

    public String getName() {
        return name;
    }

    public ProductModel setName(String name) {
        this.name = name;
        return this;
    }

    public String getBrand() {
        return brand;
    }

    public ProductModel setBrand(String brand) {
        this.brand = brand;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public ProductModel setCategory(String category) {
        this.category = category;
        return this;
    }

    public double getPrice() {
        return price;
    }

    public ProductModel setPrice(double price) {
        this.price = price;
        return this;
    }
}