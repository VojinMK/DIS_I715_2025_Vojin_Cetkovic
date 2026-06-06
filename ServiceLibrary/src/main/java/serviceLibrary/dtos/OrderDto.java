package serviceLibrary.dtos;

public class OrderDto {

    private int id;
    private String userEmail;
    private String productCode;
    private int quantity;
    private double totalPrice;
    private String status;

    public OrderDto() {
    }

    public OrderDto(int id, String userEmail, String productCode, int quantity, double totalPrice, String status) {
        this.id = id;
        this.userEmail = userEmail;
        this.productCode = productCode;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public OrderDto(String userEmail, String productCode, int quantity, double totalPrice, String status) {
        this.userEmail = userEmail;
        this.productCode = productCode;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public OrderDto setId(int id) {
        this.id = id;
        return this;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public OrderDto setUserEmail(String userEmail) {
        this.userEmail = userEmail;
        return this;
    }

    public String getProductCode() {
        return productCode;
    }

    public OrderDto setProductCode(String productCode) {
        this.productCode = productCode;
        return this;
    }

    public int getQuantity() {
        return quantity;
    }

    public OrderDto setQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public OrderDto setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public OrderDto setStatus(String status) {
        this.status = status;
        return this;
    }
}