package serviceLibrary.dtos;

public class ProductDto {

    private int id;
    private String productCode;
    private String name;
    private String brand;
    private String category;
    private double price;

    public ProductDto() {
    }

    public ProductDto(int id, String productCode, String name, String brand, String category, double price) {
        this.id = id;
        this.productCode = productCode;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.price = price;
    }

    public ProductDto(String productCode, String name, String brand, String category, double price) {
        this.productCode = productCode;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public ProductDto setId(int id) {
        this.id = id;
        return this;
    }

    public String getProductCode() {
        return productCode;
    }

    public ProductDto setProductCode(String productCode) {
        this.productCode = productCode;
        return this;
    }

    public String getName() {
        return name;
    }

    public ProductDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getBrand() {
        return brand;
    }

    public ProductDto setBrand(String brand) {
        this.brand = brand;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public ProductDto setCategory(String category) {
        this.category = category;
        return this;
    }

    public double getPrice() {
        return price;
    }

    public ProductDto setPrice(double price) {
        this.price = price;
        return this;
    }
}