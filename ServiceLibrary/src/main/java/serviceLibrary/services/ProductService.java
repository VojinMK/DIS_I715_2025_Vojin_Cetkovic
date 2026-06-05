package serviceLibrary.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import serviceLibrary.dtos.ProductDto;

public interface ProductService {

    @GetMapping("/products")
    ResponseEntity<?> getAllProducts();

    @GetMapping("/product/id")
    ResponseEntity<?> getProductById(@RequestParam int id);

    @GetMapping("/product/code")
    ResponseEntity<?> getProductByCode(@RequestParam String productCode);

    @PostMapping("/product")
    ResponseEntity<?> addProduct(@RequestBody ProductDto dto);

    @PutMapping("/product")
    ResponseEntity<?> updateProduct(@RequestBody ProductDto dto);

    @DeleteMapping("/product")
    ResponseEntity<?> deleteProduct(@RequestParam int id);
}