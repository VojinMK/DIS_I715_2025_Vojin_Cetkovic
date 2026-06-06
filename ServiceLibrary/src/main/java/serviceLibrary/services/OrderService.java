package serviceLibrary.services;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import serviceLibrary.dtos.OrderDto;

public interface OrderService {

	@GetMapping("/orders")
	ResponseEntity<?> getAllOrders();

	@GetMapping("/order/email")
	List<OrderDto> getOrdersByUserEmail(@RequestHeader("X-User-Email") String userEmail);

	@PostMapping("/order")
	OrderDto createOrder(@RequestBody OrderDto dto, @RequestHeader("X-User-Email") String userEmail);

	@PutMapping("/order")
	OrderDto updateOrder(@RequestBody OrderDto dto);

	@DeleteMapping("/order")
	ResponseEntity<?> deleteOrder(@RequestParam int id);
}