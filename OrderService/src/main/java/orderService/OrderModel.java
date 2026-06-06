package orderService;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class OrderModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(nullable = false)
	private String userEmail;

	@Column(nullable = false)
	private String productCode;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false)
	private double totalPrice;

	@Column(nullable = false)
	private String status;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime orderDate;

	public OrderModel() {
	}

	public OrderModel(String userEmail, String productCode, int quantity, double totalPrice, String status,
			LocalDateTime orderDate) {
		this.userEmail = userEmail;
		this.productCode = productCode;
		this.quantity = quantity;
		this.totalPrice = totalPrice;
		this.status = status;
		this.orderDate = orderDate;
	}

	public OrderModel(int id, String userEmail, String productCode, int quantity, double totalPrice, String status,
			LocalDateTime orderDate) {
		this.id = id;
		this.userEmail = userEmail;
		this.productCode = productCode;
		this.quantity = quantity;
		this.totalPrice = totalPrice;
		this.status = status;
		this.orderDate = orderDate;
	}

	public int getId() {
		return id;
	}

	public OrderModel setId(int id) {
		this.id = id;
		return this;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public OrderModel setUserEmail(String userEmail) {
		this.userEmail = userEmail;
		return this;
	}

	public String getProductCode() {
		return productCode;
	}

	public OrderModel setProductCode(String productCode) {
		this.productCode = productCode;
		return this;
	}

	public int getQuantity() {
		return quantity;
	}

	public OrderModel setQuantity(int quantity) {
		this.quantity = quantity;
		return this;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public OrderModel setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public OrderModel setStatus(String status) {
		this.status = status;
		return this;
	}

	public LocalDateTime getOrderDate() {
		return orderDate;
	}

	public OrderModel setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
		return this;
	}
}