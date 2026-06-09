package orderService.integrationTests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import orderService.MQConfig;
import orderService.OrderModel;
import orderService.OrderRepository;
import serviceLibrary.dtos.OrderEvent;
import serviceLibrary.dtos.ProductDto;
import serviceLibrary.dtos.StockDto;
import serviceLibrary.dtos.UserDto;
import serviceLibrary.proxies.ProductProxy;
import serviceLibrary.proxies.StockProxy;
import serviceLibrary.proxies.UserProxy;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrderServiceIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrderRepository orderRepository;

	@MockitoBean
	private UserProxy userProxy;

	@MockitoBean
	private ProductProxy productProxy;

	@MockitoBean
	private StockProxy stockProxy;

	@MockitoBean
	private RabbitTemplate rabbitTemplate;

	@BeforeEach
	void setUp() {
		orderRepository.deleteAll();
	}

	@Test
	void createOrder_ShouldCreateOrderUpdateStockAndSendEvent_WhenDataIsValid() throws Exception {
		UserDto user = new UserDto(1L, "User", "User", "user@gmail.com", "user", "USER");

		ProductDto product = new ProductDto(1, "NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99);

		StockDto stock = new StockDto(1, "NIKE-001", 10);

		when(userProxy.getUserByEmail("user@gmail.com")).thenReturn(ResponseEntity.ok(user));

		when(productProxy.getProductByCode("NIKE-001")).thenReturn(ResponseEntity.ok(product));

		when(stockProxy.getStockByProductCode("NIKE-001")).thenReturn(stock);

		String json = """
				{
				    "productCode": "NIKE-001",
				    "quantity": 2
				}
				""";

		mockMvc.perform(post("/order").header("X-User-Email", "user@gmail.com").contentType(MediaType.APPLICATION_JSON)
				.content(json)).andExpect(status().isOk()).andExpect(jsonPath("$.userEmail").value("user@gmail.com"))
				.andExpect(jsonPath("$.productCode").value("NIKE-001")).andExpect(jsonPath("$.quantity").value(2))
				.andExpect(jsonPath("$.totalPrice").value(25999.98)).andExpect(jsonPath("$.status").value("CREATED"));

		verify(stockProxy).updateStock(any(StockDto.class), eq(true));

		verify(rabbitTemplate).convertAndSend(eq(MQConfig.EXCHANGE), eq(MQConfig.ROUTING_KEY), any(OrderEvent.class));
	}

	@Test
	void createOrder_ShouldReturnError_WhenUserDoesNotExist() throws Exception {
		when(userProxy.getUserByEmail("missing@gmail.com")).thenReturn(ResponseEntity.notFound().build());

		String json = """
				{
				    "productCode": "NIKE-001",
				    "quantity": 2
				}
				""";

		mockMvc.perform(post("/order").header("X-User-Email", "missing@gmail.com")
				.contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().is4xxClientError());
	}

	@Test
	void createOrder_ShouldReturnError_WhenStockDoesNotExist() throws Exception {
		UserDto user = new UserDto(1L, "User", "User", "user@gmail.com", "user", "USER");

		ProductDto product = new ProductDto(1, "NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99);

		when(userProxy.getUserByEmail("user@gmail.com")).thenReturn(ResponseEntity.ok(user));

		when(productProxy.getProductByCode("NIKE-001")).thenReturn(ResponseEntity.ok(product));

		when(stockProxy.getStockByProductCode("NIKE-001")).thenReturn(null);

		String json = """
				{
				    "productCode": "NIKE-001",
				    "quantity": 2
				}
				""";

		mockMvc.perform(post("/order").header("X-User-Email", "user@gmail.com").contentType(MediaType.APPLICATION_JSON)
				.content(json)).andExpect(status().is4xxClientError());
	}

	@Test
	void createOrder_ShouldReturnError_WhenQuantityIsZero() throws Exception {
		UserDto user = new UserDto(1L, "User", "User", "user@gmail.com", "user", "USER");

		ProductDto product = new ProductDto(1, "NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99);

		StockDto stock = new StockDto(1, "NIKE-001", 10);

		when(userProxy.getUserByEmail("user@gmail.com")).thenReturn(ResponseEntity.ok(user));

		when(productProxy.getProductByCode("NIKE-001")).thenReturn(ResponseEntity.ok(product));

		when(stockProxy.getStockByProductCode("NIKE-001")).thenReturn(stock);

		String json = """
				{
				    "productCode": "NIKE-001",
				    "quantity": 0
				}
				""";

		mockMvc.perform(post("/order").header("X-User-Email", "user@gmail.com").contentType(MediaType.APPLICATION_JSON)
				.content(json)).andExpect(status().is4xxClientError());
	}

	@Test
	void createOrder_ShouldReturnError_WhenThereIsNotEnoughStock() throws Exception {
		UserDto user = new UserDto(1L, "User", "User", "user@gmail.com", "user", "USER");

		ProductDto product = new ProductDto(1, "NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99);

		StockDto stock = new StockDto(1, "NIKE-001", 1);

		when(userProxy.getUserByEmail("user@gmail.com")).thenReturn(ResponseEntity.ok(user));

		when(productProxy.getProductByCode("NIKE-001")).thenReturn(ResponseEntity.ok(product));

		when(stockProxy.getStockByProductCode("NIKE-001")).thenReturn(stock);

		String json = """
				{
				    "productCode": "NIKE-001",
				    "quantity": 2
				}
				""";

		mockMvc.perform(post("/order").header("X-User-Email", "user@gmail.com").contentType(MediaType.APPLICATION_JSON)
				.content(json)).andExpect(status().is4xxClientError());
	}

	@Test
	void getAllOrders_ShouldReturnOrders_WhenOrdersExist() throws Exception {
		orderRepository.save(new OrderModel("user@gmail.com", "NIKE-001", 2, 25999.98, "CREATED", LocalDateTime.now()));

		mockMvc.perform(get("/orders")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].userEmail").value("user@gmail.com"))
				.andExpect(jsonPath("$[0].productCode").value("NIKE-001")).andExpect(jsonPath("$[0].quantity").value(2))
				.andExpect(jsonPath("$[0].totalPrice").value(25999.98))
				.andExpect(jsonPath("$[0].status").value("CREATED"));
	}

	@Test
	void getAllOrders_ShouldReturnNotFound_WhenNoOrdersExist() throws Exception {
		mockMvc.perform(get("/orders")).andExpect(status().isNotFound())
				.andExpect(content().string("There is no orders."));
	}

	@Test
	void getOrdersByUserEmail_ShouldReturnOrders_WhenUserHasOrders() throws Exception {
		UserDto user = new UserDto(1L, "User", "User", "user@gmail.com", "user", "USER");

		when(userProxy.getUserByEmail("user@gmail.com")).thenReturn(ResponseEntity.ok(user));

		orderRepository.save(new OrderModel("user@gmail.com", "NIKE-001", 2, 25999.98, "CREATED", LocalDateTime.now()));

		mockMvc.perform(get("/order/email").header("X-User-Email", "user@gmail.com")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].userEmail").value("user@gmail.com"))
				.andExpect(jsonPath("$[0].productCode").value("NIKE-001"))
				.andExpect(jsonPath("$[0].quantity").value(2));
	}

	@Test
	void getOrdersByUserEmail_ShouldReturnError_WhenUserHasNoOrders() throws Exception {
		UserDto user = new UserDto(1L, "User", "User", "user@gmail.com", "user", "USER");

		when(userProxy.getUserByEmail("user@gmail.com")).thenReturn(ResponseEntity.ok(user));

		mockMvc.perform(get("/order/email").header("X-User-Email", "user@gmail.com"))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void updateOrder_ShouldUpdateOrder_WhenOrderExists() throws Exception {
		OrderModel savedOrder = orderRepository
				.save(new OrderModel("user@gmail.com", "NIKE-001", 2, 25999.98, "CREATED", LocalDateTime.now()));

		ProductDto product = new ProductDto(1, "NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99);

		when(productProxy.getProductByCode("NIKE-001")).thenReturn(ResponseEntity.ok(product));

		String json = """
				{
				    "id": %d,
				    "status": "CONFIRMED"
				}
				""".formatted(savedOrder.getId());

		mockMvc.perform(put("/order").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedOrder.getId()))
				.andExpect(jsonPath("$.productCode").value("NIKE-001")).andExpect(jsonPath("$.quantity").value(2))
				.andExpect(jsonPath("$.totalPrice").value(25999.98)).andExpect(jsonPath("$.status").value("CONFIRMED"));
	}

	@Test
	void updateOrder_ShouldReturnError_WhenOrderDoesNotExist() throws Exception {
		String json = """
				{
				    "id": 99,
				    "status": "CONFIRMED"
				}
				""";

		mockMvc.perform(put("/order").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void updateOrder_ShouldReturnError_WhenStatusDoesNotExist() throws Exception {
		OrderModel savedOrder = orderRepository
				.save(new OrderModel("user@gmail.com", "NIKE-001", 2, 25999.98, "CREATED", LocalDateTime.now()));

		String json = """
				{
				    "id": %d,
				    "status": "INVALID"
				}
				""".formatted(savedOrder.getId());

		mockMvc.perform(put("/order").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void deleteOrder_ShouldDeleteOrder_WhenOrderExists() throws Exception {
		OrderModel savedOrder = orderRepository
				.save(new OrderModel("user@gmail.com", "NIKE-001", 2, 25999.98, "CREATED", LocalDateTime.now()));

		mockMvc.perform(delete("/order").param("id", String.valueOf(savedOrder.getId()))).andExpect(status().isOk())
				.andExpect(content().string("Order witih id: " + savedOrder.getId() + " successfully deleted."));
	}

	@Test
	void deleteOrder_ShouldReturnError_WhenOrderDoesNotExist() throws Exception {
		mockMvc.perform(delete("/order").param("id", "99")).andExpect(status().is4xxClientError());
	}
}
