package serviceLibrary.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import serviceLibrary.dtos.StockDto;

public interface StockService {

	@GetMapping("/stocks")
	ResponseEntity<?> getAllStock();

	@GetMapping("/stock")
	ResponseEntity<?> getStockByProductCode(@RequestParam String productCode);

	@PostMapping("/stock")
	ResponseEntity<?> initializeStock(@RequestBody StockDto dto);

	@PutMapping("/stock")
	ResponseEntity<?> updateStock(@RequestBody StockDto dto,
			@RequestParam(defaultValue = "false") boolean fromOrder);

	@DeleteMapping("/stock")
	ResponseEntity<?> deleteStockByProductCode(@RequestParam String productCode);
}