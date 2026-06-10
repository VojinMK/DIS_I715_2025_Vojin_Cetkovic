package serviceLibrary.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import serviceLibrary.dtos.StockDto;

@FeignClient("stock-service")
public interface StockProxy {

	@PostMapping("/stock")
	ResponseEntity<?> initializeStock(@RequestBody StockDto dto);

	@DeleteMapping("/stock")
	ResponseEntity<String> deleteStockByProductCode(@RequestParam(value = "productCode") String productCode);

	@GetMapping("/stock")
	StockDto getStockByProductCode(@RequestParam(value = "productCode") String productCode);

	@PutMapping("/stock")
	ResponseEntity<?> updateStock(@RequestBody StockDto dto,
			@RequestParam(value = "fromOrder", defaultValue = "false") boolean fromOrder);
}