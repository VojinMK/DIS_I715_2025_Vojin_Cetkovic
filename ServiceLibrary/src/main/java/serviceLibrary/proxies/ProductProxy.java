package serviceLibrary.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import serviceLibrary.dtos.ProductDto;


@FeignClient("product-service")
public interface ProductProxy {

	@GetMapping("/product/code")
	ResponseEntity<ProductDto> getProductByCode(@RequestParam String productCode);
} 

