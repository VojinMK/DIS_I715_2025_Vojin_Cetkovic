package serviceLibrary.dtos;

public class StockDto {

    private int id;
    private String productCode;
    private int quantity;

    public StockDto() {
    }

    public StockDto(int id, String productCode, int quantity) {
        this.id = id;
        this.productCode = productCode;
        this.quantity = quantity;
    }

    public StockDto(String productCode, int quantity) {
        this.productCode = productCode;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public StockDto setId(int id) {
        this.id = id;
        return this;
    }

    public String getProductCode() {
        return productCode;
    }

    public StockDto setProductCode(String productCode) {
        this.productCode = productCode;
        return this;
    }

    public int getQuantity() {
        return quantity;
    }

    public StockDto setQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }
}