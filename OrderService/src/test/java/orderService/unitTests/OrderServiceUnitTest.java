package orderService.unitTests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import feign.FeignException;
import feign.Request;
import orderService.MQConfig;
import orderService.OrderModel;
import orderService.OrderRepository;
import orderService.OrderServiceImpl;
import serviceLibrary.dtos.OrderDto;
import serviceLibrary.dtos.OrderEvent;
import serviceLibrary.dtos.ProductDto;
import serviceLibrary.dtos.StockDto;
import serviceLibrary.dtos.UserDto;
import serviceLibrary.proxies.ProductProxy;
import serviceLibrary.proxies.StockProxy;
import serviceLibrary.proxies.UserProxy;
import util.exceptions.DataIntegrityViolationException;
import util.exceptions.NoDataFoundException;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUnitTest {
	@Mock
	private OrderRepository orderRepository;

	@Mock
	private UserProxy userProxy;

	@Mock
	private ProductProxy productProxy;

	@Mock
	private StockProxy stockProxy;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private OrderServiceImpl orderService;

	private UserDto userDto;
	private ProductDto productDto;
	private StockDto stockDto;
	private OrderModel orderModel;

	@BeforeEach
	void setUp() {
		userDto = new UserDto(1L, "User", "User", "user@gmail.com", "password", "USER");

		productDto = new ProductDto(1, "NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99);

		stockDto = new StockDto(1, "NIKE-001", 10);

		orderModel = new OrderModel(1, "user@gmail.com", "NIKE-001", 2, 25999.98, "CREATED", LocalDateTime.now());
	}

	@Test
	void createOrder_ShouldCreateOrderUpdateStockAndSendEvent_WhenDataIsValid() {
		OrderDto request = new OrderDto();
		request.setProductCode("NIKE-001");
		request.setQuantity(2);

		when(userProxy.getUserByEmail("user@gmail.com")).thenReturn(ResponseEntity.ok(userDto));

		when(productProxy.getProductByCode("NIKE-001")).thenReturn(ResponseEntity.ok(productDto));

		when(stockProxy.getStockByProductCode("NIKE-001")).thenReturn(stockDto);

		when(orderRepository.save(any(OrderModel.class))).thenReturn(orderModel);

		OrderDto result = orderService.createOrder(request, "user@gmail.com");

		assertNotNull(result);
		assertEquals(1, result.getId());
		assertEquals("user@gmail.com", result.getUserEmail());
		assertEquals("NIKE-001", result.getProductCode());
		assertEquals(2, result.getQuantity());
		assertEquals(25999.98, result.getTotalPrice());
		assertEquals("CREATED", result.getStatus());

		verify(userProxy, times(1)).getUserByEmail("user@gmail.com");
		verify(productProxy, times(1)).getProductByCode("NIKE-001");
		verify(stockProxy, times(1)).getStockByProductCode("NIKE-001");

		ArgumentCaptor<OrderModel> orderCaptor = ArgumentCaptor.forClass(OrderModel.class);
		verify(orderRepository, times(1)).save(orderCaptor.capture());

		OrderModel savedArgument = orderCaptor.getValue();
		assertEquals("user@gmail.com", savedArgument.getUserEmail());
		assertEquals("NIKE-001", savedArgument.getProductCode());
		assertEquals(2, savedArgument.getQuantity());
		assertEquals(25999.98, savedArgument.getTotalPrice());
		assertEquals("CREATED", savedArgument.getStatus());
		assertNotNull(savedArgument.getOrderDate());

		ArgumentCaptor<StockDto> stockCaptor = ArgumentCaptor.forClass(StockDto.class);
		verify(stockProxy, times(1)).updateStock(stockCaptor.capture(), eq(true));

		StockDto updatedStock = stockCaptor.getValue();
		assertEquals(1, updatedStock.getId());
		assertEquals("NIKE-001", updatedStock.getProductCode());
		assertEquals(8, updatedStock.getQuantity());

		ArgumentCaptor<OrderEvent> eventCaptor = ArgumentCaptor.forClass(OrderEvent.class);
		verify(rabbitTemplate, times(1)).convertAndSend(eq(MQConfig.EXCHANGE), eq(MQConfig.ROUTING_KEY),
				eventCaptor.capture());

		OrderEvent event = eventCaptor.getValue();
		assertNotNull(event.getMessageId());
		assertEquals("user@gmail.com", event.getCustomerEmail());
		assertEquals(1, event.getOrderId());
		assertEquals(25999.98, event.getTotalAmount());
		assertEquals("Your order has been created successfully.", event.getMessage());
		assertNotNull(event.getEventDate());
	}

	@Test
	void createOrder_ShouldThrowNoDataFoundException_WhenUserResponseIsNotFound() {
		OrderDto request = new OrderDto();
		request.setProductCode("NIKE-001");
		request.setQuantity(2);

		when(userProxy.getUserByEmail("user@gmail.com"))
				.thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> orderService.createOrder(request, "user@gmail.com"));

		assertEquals("User with email user@gmail.com does not exist.", exception.getMessage());

		verify(userProxy, times(1)).getUserByEmail("user@gmail.com");
		verify(productProxy, never()).getProductByCode(anyString());
		verify(stockProxy, never()).getStockByProductCode(anyString());
		verify(orderRepository, never()).save(any(OrderModel.class));
		verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(OrderEvent.class));
	}

	@Test
	void createOrder_ShouldThrowNoDataFoundException_WhenUserBodyIsNull() {
		OrderDto request = new OrderDto();
		request.setProductCode("NIKE-001");
		request.setQuantity(2);

		when(userProxy.getUserByEmail("user@gmail.com")).thenReturn(ResponseEntity.ok(null));

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> orderService.createOrder(request, "user@gmail.com"));

		assertEquals("User with email user@gmail.com does not exist.", exception.getMessage());

		verify(userProxy, times(1)).getUserByEmail("user@gmail.com");
		verify(productProxy, never()).getProductByCode(anyString());
		verify(orderRepository, never()).save(any(OrderModel.class));
	}

	@Test
	void createOrder_ShouldThrowNoDataFoundException_WhenProductFeignReturnsNotFound() {
		OrderDto request = new OrderDto();
		request.setProductCode("MISSING-001");
		request.setQuantity(2);

		when(userProxy.getUserByEmail("user@gmail.com")).thenReturn(ResponseEntity.ok(userDto));

		when(productProxy.getProductByCode("MISSING-001")).thenThrow(feignNotFound());

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> orderService.createOrder(request, "user@gmail.com"));

		assertEquals("Product with code MISSING-001 does not exist.", exception.getMessage());

		verify(userProxy, times(1)).getUserByEmail("user@gmail.com");
		verify(productProxy, times(1)).getProductByCode("MISSING-001");
		verify(stockProxy, never()).getStockByProductCode(anyString());
		verify(orderRepository, never()).save(any(OrderModel.class));
	}

	@Test
	void createOrder_ShouldThrowNoDataFoundException_WhenStockDoesNotExist() {
		OrderDto request = new OrderDto();
		request.setProductCode("NIKE-001");
		request.setQuantity(2);

		when(userProxy.getUserByEmail("user@gmail.com")).thenReturn(ResponseEntity.ok(userDto));

		when(productProxy.getProductByCode("NIKE-001")).thenReturn(ResponseEntity.ok(productDto));

		when(stockProxy.getStockByProductCode("NIKE-001")).thenReturn(null);

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> orderService.createOrder(request, "user@gmail.com"));

		assertEquals("Stock for product code NIKE-001 does not exist.", exception.getMessage());

		verify(stockProxy, times(1)).getStockByProductCode("NIKE-001");
		verify(orderRepository, never()).save(any(OrderModel.class));
		verify(stockProxy, never()).updateStock(any(StockDto.class), anyBoolean());
		verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(OrderEvent.class));
	}

	@Test
	void createOrder_ShouldThrowDataIntegrityViolationException_WhenQuantityIsZero() {
		OrderDto request = new OrderDto();
		request.setProductCode("NIKE-001");
		request.setQuantity(0);

		when(userProxy.getUserByEmail("user@gmail.com")).thenReturn(ResponseEntity.ok(userDto));

		when(productProxy.getProductByCode("NIKE-001")).thenReturn(ResponseEntity.ok(productDto));

		when(stockProxy.getStockByProductCode("NIKE-001")).thenReturn(stockDto);

		DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
				() -> orderService.createOrder(request, "user@gmail.com"));

		assertEquals("Quantity must be greater than zero.", exception.getMessage());

		verify(orderRepository, never()).save(any(OrderModel.class));
		verify(stockProxy, never()).updateStock(any(StockDto.class), anyBoolean());
		verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(OrderEvent.class));
	}

	@Test
	void createOrder_ShouldThrowDataIntegrityViolationException_WhenQuantityIsNegative() {
		OrderDto request = new OrderDto();
		request.setProductCode("NIKE-001");
		request.setQuantity(-1);

		when(userProxy.getUserByEmail("user@gmail.com")).thenReturn(ResponseEntity.ok(userDto));

		when(productProxy.getProductByCode("NIKE-001")).thenReturn(ResponseEntity.ok(productDto));

		when(stockProxy.getStockByProductCode("NIKE-001")).thenReturn(stockDto);

		DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
				() -> orderService.createOrder(request, "user@gmail.com"));

		assertEquals("Quantity must be greater than zero.", exception.getMessage());

		verify(orderRepository, never()).save(any(OrderModel.class));
		verify(stockProxy, never()).updateStock(any(StockDto.class), anyBoolean());
	}

	@Test
	void createOrder_ShouldThrowDataIntegrityViolationException_WhenThereIsNotEnoughStock() {
		OrderDto request = new OrderDto();
		request.setProductCode("NIKE-001");
		request.setQuantity(20);

		when(userProxy.getUserByEmail("user@gmail.com")).thenReturn(ResponseEntity.ok(userDto));

		when(productProxy.getProductByCode("NIKE-001")).thenReturn(ResponseEntity.ok(productDto));

		when(stockProxy.getStockByProductCode("NIKE-001")).thenReturn(stockDto);

		DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
				() -> orderService.createOrder(request, "user@gmail.com"));

		assertEquals("Not enough stock for product code NIKE-001.", exception.getMessage());

		verify(orderRepository, never()).save(any(OrderModel.class));
		verify(stockProxy, never()).updateStock(any(StockDto.class), anyBoolean());
		verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(OrderEvent.class));
	}

	@Test
	void getAllOrders_ShouldReturnOrders_WhenOrdersExist() {
		when(orderRepository.findAll()).thenReturn(List.of(orderModel));

		ResponseEntity<?> response = orderService.getAllOrders();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		@SuppressWarnings("unchecked")
		List<OrderDto> orders = (List<OrderDto>) response.getBody();

		assertEquals(1, orders.size());
		assertEquals(1, orders.get(0).getId());
		assertEquals("user@gmail.com", orders.get(0).getUserEmail());
		assertEquals("NIKE-001", orders.get(0).getProductCode());
		assertEquals(2, orders.get(0).getQuantity());
		assertEquals(25999.98, orders.get(0).getTotalPrice());
		assertEquals("CREATED", orders.get(0).getStatus());

		verify(orderRepository, times(1)).findAll();
	}

	@Test
	void getAllOrders_ShouldReturnNotFound_WhenThereAreNoOrders() {
		when(orderRepository.findAll()).thenReturn(List.of());

		ResponseEntity<?> response = orderService.getAllOrders();

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals("There is no orders.", response.getBody());

		verify(orderRepository, times(1)).findAll();
	}

	@Test
	void getOrdersByUserEmail_ShouldReturnOrders_WhenUserAndOrdersExist() {
		when(userProxy.getUserByEmail("user@gmail.com")).thenReturn(ResponseEntity.ok(userDto));

		when(orderRepository.findByUserEmail("user@gmail.com")).thenReturn(List.of(orderModel));

		List<OrderDto> result = orderService.getOrdersByUserEmail("user@gmail.com");

		assertEquals(1, result.size());
		assertEquals(1, result.get(0).getId());
		assertEquals("user@gmail.com", result.get(0).getUserEmail());
		assertEquals("NIKE-001", result.get(0).getProductCode());
		assertEquals(2, result.get(0).getQuantity());
		assertEquals(25999.98, result.get(0).getTotalPrice());
		assertEquals("CREATED", result.get(0).getStatus());

		verify(userProxy, times(1)).getUserByEmail("user@gmail.com");
		verify(orderRepository, times(1)).findByUserEmail("user@gmail.com");
	}

	@Test
	void getOrdersByUserEmail_ShouldThrowNoDataFoundException_WhenUserFeignReturnsNotFound() {
		when(userProxy.getUserByEmail("missing@gmail.com")).thenThrow(feignNotFound());

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> orderService.getOrdersByUserEmail("missing@gmail.com"));

		assertEquals("User with email missing@gmail.com does not exist.", exception.getMessage());

		verify(userProxy, times(1)).getUserByEmail("missing@gmail.com");
		verify(orderRepository, never()).findByUserEmail(anyString());
	}

	@Test
	void getOrdersByUserEmail_ShouldThrowNoDataFoundException_WhenUserBodyIsNull() {
		when(userProxy.getUserByEmail("missing@gmail.com")).thenReturn(ResponseEntity.ok(null));

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> orderService.getOrdersByUserEmail("missing@gmail.com"));

		assertEquals("User with email missing@gmail.com does not exist.", exception.getMessage());

		verify(orderRepository, never()).findByUserEmail(anyString());
	}

	@Test
	void getOrdersByUserEmail_ShouldThrowNoDataFoundException_WhenUserHasNoOrders() {
		when(userProxy.getUserByEmail("user@gmail.com")).thenReturn(ResponseEntity.ok(userDto));

		when(orderRepository.findByUserEmail("user@gmail.com")).thenReturn(List.of());

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> orderService.getOrdersByUserEmail("user@gmail.com"));

		assertEquals("User with email user@gmail.com does not have orders.", exception.getMessage());

		verify(userProxy, times(1)).getUserByEmail("user@gmail.com");
		verify(orderRepository, times(1)).findByUserEmail("user@gmail.com");
	}

	@Test
	void updateOrder_ShouldUpdateOnlyProvidedFields_WhenOrderExists() {
		OrderDto request = new OrderDto();
		request.setId(1);
		request.setStatus("confirmed");

		OrderModel updatedOrder = new OrderModel(1, "user@gmail.com", "NIKE-001", 2, 25999.98, "CONFIRMED",
				LocalDateTime.now());

		when(orderRepository.findById(1)).thenReturn(orderModel).thenReturn(updatedOrder);

		when(productProxy.getProductByCode("NIKE-001")).thenReturn(ResponseEntity.ok(productDto));

		OrderDto result = orderService.updateOrder(request);

		assertNotNull(result);
		assertEquals(1, result.getId());
		assertEquals("user@gmail.com", result.getUserEmail());
		assertEquals("NIKE-001", result.getProductCode());
		assertEquals(2, result.getQuantity());
		assertEquals(25999.98, result.getTotalPrice());
		assertEquals("CONFIRMED", result.getStatus());

		verify(orderRepository, times(2)).findById(1);
		verify(productProxy, times(1)).getProductByCode("NIKE-001");
		verify(orderRepository, times(1)).updateOrder(1, "NIKE-001", 2, 25999.98, "CONFIRMED");
	}

	@Test
	void updateOrder_ShouldUpdateProductAndQuantityAndRecalculateTotalPrice_WhenFieldsAreProvided() {
		OrderDto request = new OrderDto();
		request.setId(1);
		request.setProductCode("ADIDAS-001");
		request.setQuantity(3);

		ProductDto adidasProduct = new ProductDto(2, "ADIDAS-001", "Adidas Shorts", "Adidas", "CLOTHING", 5000.0);

		OrderModel updatedOrder = new OrderModel(1, "user@gmail.com", "ADIDAS-001", 3, 15000.0, "CREATED",
				LocalDateTime.now());

		when(orderRepository.findById(1)).thenReturn(orderModel).thenReturn(updatedOrder);

		when(productProxy.getProductByCode("ADIDAS-001")).thenReturn(ResponseEntity.ok(adidasProduct));

		OrderDto result = orderService.updateOrder(request);

		assertNotNull(result);
		assertEquals("ADIDAS-001", result.getProductCode());
		assertEquals(3, result.getQuantity());
		assertEquals(15000.0, result.getTotalPrice());
		assertEquals("CREATED", result.getStatus());

		verify(orderRepository, times(1)).updateOrder(1, "ADIDAS-001", 3, 15000.0, "CREATED");
	}

	@Test
	void updateOrder_ShouldThrowNoDataFoundException_WhenOrderDoesNotExist() {
		OrderDto request = new OrderDto();
		request.setId(99);
		request.setStatus("CONFIRMED");

		when(orderRepository.findById(99)).thenReturn(null);

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> orderService.updateOrder(request));

		assertEquals("Order with id: 99 does not exist.", exception.getMessage());

		verify(orderRepository, times(1)).findById(99);
		verify(productProxy, never()).getProductByCode(anyString());
		verify(orderRepository, never()).updateOrder(anyInt(), anyString(), anyInt(), anyDouble(), anyString());
	}

	@Test
	void updateOrder_ShouldThrowDataIntegrityViolationException_WhenQuantityIsZeroAfterUpdate() {
		OrderModel existingOrderWithZeroQuantity = new OrderModel(1, "user@gmail.com", "NIKE-001", 0, 0.0, "CREATED",
				LocalDateTime.now());

		OrderDto request = new OrderDto();
		request.setId(1);

		when(orderRepository.findById(1)).thenReturn(existingOrderWithZeroQuantity);

		DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
				() -> orderService.updateOrder(request));

		assertEquals("Quantity must be greater than zero.", exception.getMessage());

		verify(productProxy, never()).getProductByCode(anyString());
		verify(orderRepository, never()).updateOrder(anyInt(), anyString(), anyInt(), anyDouble(), anyString());
	}

	@Test
	void updateOrder_ShouldThrowDataIntegrityViolationException_WhenStatusDoesNotExist() {
		OrderDto request = new OrderDto();
		request.setId(1);
		request.setStatus("INVALID");

		when(orderRepository.findById(1)).thenReturn(orderModel);

		DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
				() -> orderService.updateOrder(request));

		assertEquals("Order status does not exist.", exception.getMessage());

		verify(productProxy, never()).getProductByCode(anyString());
		verify(orderRepository, never()).updateOrder(anyInt(), anyString(), anyInt(), anyDouble(), anyString());
	}

	@Test
	void updateOrder_ShouldThrowNoDataFoundException_WhenProductResponseBodyIsNull() {
		OrderDto request = new OrderDto();
		request.setId(1);
		request.setProductCode("MISSING-001");
		request.setQuantity(2);

		when(orderRepository.findById(1)).thenReturn(orderModel);

		when(productProxy.getProductByCode("MISSING-001")).thenReturn(ResponseEntity.ok(null));

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> orderService.updateOrder(request));

		assertEquals("Product with code MISSING-001 does not exist.", exception.getMessage());

		verify(orderRepository, never()).updateOrder(anyInt(), anyString(), anyInt(), anyDouble(), anyString());
	}

	@Test
	void updateOrder_ShouldThrowNoDataFoundException_WhenOrderDisappearsAfterUpdate() {
		OrderDto request = new OrderDto();
		request.setId(1);
		request.setStatus("CONFIRMED");

		when(orderRepository.findById(1)).thenReturn(orderModel).thenReturn(null);

		when(productProxy.getProductByCode("NIKE-001")).thenReturn(ResponseEntity.ok(productDto));

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> orderService.updateOrder(request));

		assertEquals("Order with id 1 does not exist.", exception.getMessage());

		verify(orderRepository, times(1)).updateOrder(1, "NIKE-001", 2, 25999.98, "CONFIRMED");
	}

	@Test
	void deleteOrder_ShouldDeleteOrder_WhenOrderExists() {
		when(orderRepository.findById(1)).thenReturn(orderModel);

		ResponseEntity<?> response = orderService.deleteOrder(1);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Order witih id: 1 successfully deleted.", response.getBody());

		verify(orderRepository, times(1)).findById(1);
		verify(orderRepository, times(1)).delete(orderModel);
	}

	@Test
	void deleteOrder_ShouldThrowNoDataFoundException_WhenOrderDoesNotExist() {
		when(orderRepository.findById(99)).thenReturn(null);

		NoDataFoundException exception = assertThrows(NoDataFoundException.class, () -> orderService.deleteOrder(99));

		assertEquals("Order with this id: 99 does not exist.", exception.getMessage());

		verify(orderRepository, times(1)).findById(99);
		verify(orderRepository, never()).delete(any(OrderModel.class));
	}

	private FeignException.NotFound feignNotFound() {
		Request request = Request.create(Request.HttpMethod.GET, "/test", Collections.emptyMap(), null,
				StandardCharsets.UTF_8, null);

		return new FeignException.NotFound("Not found", request, null, Collections.emptyMap());
	}
}
