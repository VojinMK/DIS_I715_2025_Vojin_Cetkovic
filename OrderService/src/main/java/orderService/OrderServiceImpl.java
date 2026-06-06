package orderService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import serviceLibrary.dtos.OrderDto;
import serviceLibrary.dtos.ProductDto;
import serviceLibrary.dtos.StockDto;
import serviceLibrary.dtos.UserDto;
import serviceLibrary.proxies.ProductProxy;
import serviceLibrary.proxies.StockProxy;
import serviceLibrary.proxies.UserProxy;
import serviceLibrary.services.OrderService;
import util.exceptions.DataIntegrityViolationException;
import util.exceptions.NoDataFoundException;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import serviceLibrary.dtos.OrderEvent;

@RestController
public class OrderServiceImpl implements OrderService {

	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private UserProxy userProxy;
	@Autowired
	private ProductProxy productProxy;
	@Autowired
	private StockProxy stockProxy;
	@Autowired
	private  RabbitTemplate rabbitTemplate;
	
	@Override
	public OrderDto createOrder(OrderDto dto, String userEmail) {

		ResponseEntity<ProductDto> productResponse;
	    ResponseEntity<UserDto> userResponse = userProxy.getUserByEmail(userEmail);
	    UserDto user = userResponse.getBody();

	    if (userResponse.getStatusCode().is4xxClientError() || user == null) {
	        throw new NoDataFoundException("User with email " + userEmail + " does not exist.");
	    }

	    try {
	        productResponse = productProxy.getProductByCode(dto.getProductCode());
	    } catch (feign.FeignException.NotFound e) {
	        throw new NoDataFoundException("Product with code " + dto.getProductCode() + " does not exist.");
	    }
	    
	    ProductDto product = productResponse.getBody();

	    StockDto stock = stockProxy.getStockByProductCode(dto.getProductCode());

	    if (stock == null) {
	        throw new NoDataFoundException("Stock for product code " + dto.getProductCode() + " does not exist.");
	    }

	    if (dto.getQuantity() <= 0) {
	        throw new DataIntegrityViolationException("Quantity must be greater than zero.");
	    }

	    if (stock.getQuantity() < dto.getQuantity()) {
	        throw new DataIntegrityViolationException("Not enough stock for product code " + dto.getProductCode() + ".");
	    }

	    double totalPrice = product.getPrice() * dto.getQuantity();

	    OrderModel order = new OrderModel(userEmail, dto.getProductCode(), dto.getQuantity(), totalPrice, "CREATED",
	            LocalDateTime.now());

	    OrderModel savedOrder = orderRepository.save(order);

	    int newQuantity = stock.getQuantity() - dto.getQuantity();

	    StockDto updateStockDto = new StockDto(stock.getId(), stock.getProductCode(), newQuantity);

	    stockProxy.updateStock(updateStockDto,true);
	    
	    OrderEvent event = new OrderEvent(
	            UUID.randomUUID().toString(),
	            userEmail,
	            savedOrder.getId(),
	            savedOrder.getTotalPrice(),
	            "Your order has been created successfully.",
	            LocalDateTime.now()
	    );

	    rabbitTemplate.convertAndSend(
	            MQConfig.EXCHANGE,
	            MQConfig.ROUTING_KEY,
	            event
	    );

	    return convertModelToDto(savedOrder);
	}

	@Override
	public ResponseEntity<?> getAllOrders() {
		List<OrderModel> orderModels = orderRepository.findAll();
		List<OrderDto> orderDtos = new ArrayList<>();

		for (OrderModel model : orderModels) { 
			orderDtos.add(convertModelToDto(model));
		}

		if (orderDtos.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no orders.");
		}

		return ResponseEntity.ok(orderDtos);
	}

	@Override
	public List<OrderDto> getOrdersByUserEmail(String email) {
		ResponseEntity<UserDto> response;
		try {
			response = userProxy.getUserByEmail(email);
		} catch (feign.FeignException.NotFound e) {
			throw new NoDataFoundException("User with email " + email + " does not exist.");
		}

		UserDto user = response.getBody();

		if (user == null) {
			throw new NoDataFoundException("User with email " + email + " does not exist.");
		}

		List<OrderModel> orderModels = orderRepository.findByUserEmail(email);
		List<OrderDto> orderDtos = new ArrayList<>();

		for (OrderModel model : orderModels) {
			orderDtos.add(convertModelToDto(model));
		}

		if (orderDtos.isEmpty()) {
			throw new NoDataFoundException("User with email " + email + " does not have orders.");
		}

		return orderDtos;
	}

	@Override
	public OrderDto updateOrder(OrderDto dto) {
		OrderModel existingOrder = orderRepository.findById(dto.getId());
		
		if (existingOrder == null) {
			throw new NoDataFoundException("Order with id: " + dto.getId() + " does not exist.");
		}

		Set<String> allowedStatuses = Set.of("CREATED", "CONFIRMED", "CANCELLED", "DELIVERED");

		String productCode = dto.getProductCode() != null ? dto.getProductCode() : existingOrder.getProductCode();


		int quantity = dto.getQuantity() != 0 ? dto.getQuantity() : existingOrder.getQuantity();
		
		if(quantity<=0) {
			throw new DataIntegrityViolationException("Quantity must be greater than zero.");
		}

		String status = dto.getStatus() != null ? dto.getStatus().toUpperCase() : existingOrder.getStatus();

		if (!allowedStatuses.contains(status)) {
			throw new DataIntegrityViolationException("Order status does not exist.");
		}

		ResponseEntity<ProductDto> response = productProxy.getProductByCode(productCode);
		ProductDto product=response.getBody();

		if (product == null || response.getStatusCode().is4xxClientError()) {
			throw new NoDataFoundException("Product with code " + productCode + " does not exist.");
		}

		double totalPrice = product.getPrice() * quantity;

		orderRepository.updateOrder(dto.getId(), productCode, quantity, totalPrice, status);

		OrderModel updatedOrder = orderRepository.findById(dto.getId());
		if(updatedOrder==null) {
			throw new NoDataFoundException("Order with id " + dto.getId() + " does not exist.");
		}

		return convertModelToDto(updatedOrder);
	}

	@Override
	public ResponseEntity<?> deleteOrder(int id) {
		OrderModel order = orderRepository.findById(id);
		
		if (order == null) {
		    throw new NoDataFoundException("Order with this id: " + id + " does not exist.");
		}

		orderRepository.delete(order);
		return ResponseEntity.ok("Order witih id: " + id + " successfully deleted.");
	}

	private OrderDto convertModelToDto(OrderModel model) {
		return new OrderDto(model.getId(), model.getUserEmail(), model.getProductCode(), model.getQuantity(),
				model.getTotalPrice(), model.getStatus());
	}
} 
