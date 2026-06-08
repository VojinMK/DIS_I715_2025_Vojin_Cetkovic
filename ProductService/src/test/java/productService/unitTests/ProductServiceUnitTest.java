package productService.unitTests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import productService.ProductModel;
import productService.ProductRepository;
import productService.ProductServiceImpl;
import serviceLibrary.dtos.ProductDto;
import serviceLibrary.dtos.StockDto;
import serviceLibrary.proxies.StockProxy;
import util.exceptions.ConflictException;
import util.exceptions.NoDataFoundException;

@ExtendWith(MockitoExtension.class)
public class ProductServiceUnitTest {
	@Mock
	private ProductRepository productRepository;

	@Mock
	private StockProxy stockProxy;

	@InjectMocks
	private ProductServiceImpl productService;

	private ProductModel productModel;

	@BeforeEach
	void setUp() {
		productModel = new ProductModel(1, "NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99);
	}

	@Test
	void getAllProducts_ShouldReturnProducts_WhenProductsExist() {
		when(productRepository.findAll()).thenReturn(List.of(productModel));

		ResponseEntity<?> response = productService.getAllProducts();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		@SuppressWarnings("unchecked")
		List<ProductDto> products = (List<ProductDto>) response.getBody();

		assertEquals(1, products.size());
		assertEquals(1, products.get(0).getId());
		assertEquals("NIKE-001", products.get(0).getProductCode());
		assertEquals("Nike Air Max", products.get(0).getName());
		assertEquals("Nike", products.get(0).getBrand());
		assertEquals("FOOTWEAR", products.get(0).getCategory());
		assertEquals(12999.99, products.get(0).getPrice());

		verify(productRepository, times(1)).findAll();
	}

	@Test
	void getAllProducts_ShouldReturnNotFound_WhenThereAreNoProducts() {
		when(productRepository.findAll()).thenReturn(List.of());

		ResponseEntity<?> response = productService.getAllProducts();

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals("There is no products.", response.getBody());

		verify(productRepository, times(1)).findAll();
	}

	@Test
	void getProductById_ShouldReturnProduct_WhenProductExists() {
		when(productRepository.findById(1)).thenReturn(productModel);

		ResponseEntity<?> response = productService.getProductById(1);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		ProductDto dto = (ProductDto) response.getBody();

		assertEquals(1, dto.getId());
		assertEquals("NIKE-001", dto.getProductCode());
		assertEquals("Nike Air Max", dto.getName());
		assertEquals("Nike", dto.getBrand());
		assertEquals("FOOTWEAR", dto.getCategory());
		assertEquals(12999.99, dto.getPrice());

		verify(productRepository, times(1)).findById(1);
	}

	@Test
	void getProductById_ShouldThrowNoDataFoundException_WhenProductDoesNotExist() {
		when(productRepository.findById(99)).thenReturn(null);

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> productService.getProductById(99));

		assertEquals("Product with id 99 not found.", exception.getMessage());

		verify(productRepository, times(1)).findById(99);
	}

	@Test
	void getProductByCode_ShouldReturnProduct_WhenProductExists() {
		when(productRepository.findByProductCode("NIKE-001")).thenReturn(productModel);

		ResponseEntity<?> response = productService.getProductByCode("NIKE-001");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		ProductDto dto = (ProductDto) response.getBody();

		assertEquals(1, dto.getId());
		assertEquals("NIKE-001", dto.getProductCode());
		assertEquals("Nike Air Max", dto.getName());
		assertEquals("Nike", dto.getBrand());
		assertEquals("FOOTWEAR", dto.getCategory());
		assertEquals(12999.99, dto.getPrice());

		verify(productRepository, times(1)).findByProductCode("NIKE-001");
	}

	@Test
	void getProductByCode_ShouldThrowNoDataFoundException_WhenProductDoesNotExist() {
		when(productRepository.findByProductCode("MISSING-001")).thenReturn(null);

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> productService.getProductByCode("MISSING-001"));

		assertEquals("Product with code MISSING-001 not found.", exception.getMessage());

		verify(productRepository, times(1)).findByProductCode("MISSING-001");
	}

	@Test
	void addProduct_ShouldCreateProductAndInitializeStock_WhenProductCodeDoesNotExist() {
		ProductDto request = new ProductDto("NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99);

		ProductModel savedProduct = new ProductModel(1, "NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99);

		when(productRepository.existsByProductCode("NIKE-001")).thenReturn(false);
		when(productRepository.save(any(ProductModel.class))).thenReturn(savedProduct);
		when(stockProxy.initializeStock(any(StockDto.class)))
        .thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CREATED).body("Stock initialized."));

		ResponseEntity<?> response = productService.addProduct(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());

		ProductDto dto = (ProductDto) response.getBody();

		assertEquals(1, dto.getId());
		assertEquals("NIKE-001", dto.getProductCode());
		assertEquals("Nike Air Max", dto.getName());
		assertEquals("Nike", dto.getBrand());
		assertEquals("FOOTWEAR", dto.getCategory());
		assertEquals(12999.99, dto.getPrice());

		verify(productRepository, times(1)).existsByProductCode("NIKE-001");
		verify(productRepository, times(1)).save(any(ProductModel.class));
		verify(stockProxy, times(1)).initializeStock(any(StockDto.class));
		verify(productRepository, never()).delete(any(ProductModel.class));
	}

	@Test
	void addProduct_ShouldThrowConflictException_WhenProductCodeAlreadyExists() {
		ProductDto request = new ProductDto("NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99);

		when(productRepository.existsByProductCode("NIKE-001")).thenReturn(true);

		ConflictException exception = assertThrows(ConflictException.class, () -> productService.addProduct(request));

		assertEquals("Product with code NIKE-001 already exists.", exception.getMessage());

		verify(productRepository, times(1)).existsByProductCode("NIKE-001");
		verify(productRepository, never()).save(any(ProductModel.class));
		verify(stockProxy, never()).initializeStock(any(StockDto.class));
	}

	@Test
	void addProduct_ShouldDeleteProductAndThrowRuntimeException_WhenStockInitializationFails() {
		ProductDto request = new ProductDto("NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99);

		ProductModel savedProduct = new ProductModel(1, "NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99);

		when(productRepository.existsByProductCode("NIKE-001")).thenReturn(false);
		when(productRepository.save(any(ProductModel.class))).thenReturn(savedProduct);
		when(stockProxy.initializeStock(any(StockDto.class)))
        .thenReturn((ResponseEntity) ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Stock error."));

		RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.addProduct(request));

		assertEquals("Product created, but failed to initialize stock. Product deleted.", exception.getMessage());

		verify(productRepository, times(1)).existsByProductCode("NIKE-001");
		verify(productRepository, times(1)).save(any(ProductModel.class));
		verify(stockProxy, times(1)).initializeStock(any(StockDto.class));
		verify(productRepository, times(1)).delete(savedProduct);
	}

	@Test
	void updateProduct_ShouldUpdateOnlyProvidedFields_WhenProductExists() {
		ProductDto request = new ProductDto();
		request.setProductCode("NIKE-001");
		request.setName("Nike Air Max Updated");
		request.setPrice(13999.99);

		ProductModel updatedProduct = new ProductModel(1, "NIKE-001", "Nike Air Max Updated", "Nike", "FOOTWEAR",
				13999.99);

		when(productRepository.findByProductCode("NIKE-001")).thenReturn(productModel).thenReturn(updatedProduct);

		ResponseEntity<?> response = productService.updateProduct(request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		ProductDto dto = (ProductDto) response.getBody();

		assertEquals(1, dto.getId());
		assertEquals("NIKE-001", dto.getProductCode());
		assertEquals("Nike Air Max Updated", dto.getName());
		assertEquals("Nike", dto.getBrand());
		assertEquals("FOOTWEAR", dto.getCategory());
		assertEquals(13999.99, dto.getPrice());

		verify(productRepository, times(2)).findByProductCode("NIKE-001");
		verify(productRepository, times(1)).updateProduct("NIKE-001", "Nike Air Max Updated", "Nike", "FOOTWEAR",
				13999.99);
	}

	@Test
	void updateProduct_ShouldKeepOldValues_WhenFieldsAreNullOrPriceIsZero() {
		ProductDto request = new ProductDto();
		request.setProductCode("NIKE-001");

		when(productRepository.findByProductCode("NIKE-001")).thenReturn(productModel).thenReturn(productModel);

		ResponseEntity<?> response = productService.updateProduct(request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		ProductDto dto = (ProductDto) response.getBody();

		assertEquals(1, dto.getId());
		assertEquals("NIKE-001", dto.getProductCode());
		assertEquals("Nike Air Max", dto.getName());
		assertEquals("Nike", dto.getBrand());
		assertEquals("FOOTWEAR", dto.getCategory());
		assertEquals(12999.99, dto.getPrice());

		verify(productRepository, times(2)).findByProductCode("NIKE-001");
		verify(productRepository, times(1)).updateProduct("NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99);
	}

	@Test
	void updateProduct_ShouldThrowNoDataFoundException_WhenProductDoesNotExist() {
		ProductDto request = new ProductDto();
		request.setProductCode("MISSING-001");
		request.setName("Missing Product");

		when(productRepository.findByProductCode("MISSING-001")).thenReturn(null);

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> productService.updateProduct(request));

		assertEquals("Product with code MISSING-001 does not exist.", exception.getMessage());

		verify(productRepository, times(1)).findByProductCode("MISSING-001");
		verify(productRepository, never()).updateProduct(anyString(), anyString(), anyString(), anyString(),
				anyDouble());
	}

	@Test
	void deleteProduct_ShouldDeleteStockFirstAndThenDeleteProduct_WhenProductExists() {
		when(productRepository.findById(1)).thenReturn(productModel);
		when(stockProxy.deleteStockByProductCode("NIKE-001")).thenReturn(ResponseEntity.ok("Stock deleted."));

		ResponseEntity<?> response = productService.deleteProduct(1);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Product and stock successfully deleted.", response.getBody());

		verify(productRepository, times(1)).findById(1);
		verify(stockProxy, times(1)).deleteStockByProductCode("NIKE-001");
		verify(productRepository, times(1)).deleteById(1);
	}

	@Test
	void deleteProduct_ShouldThrowNoDataFoundException_WhenProductDoesNotExist() {
		when(productRepository.findById(99)).thenReturn(null);

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> productService.deleteProduct(99));

		assertEquals("Product not found.", exception.getMessage());

		verify(productRepository, times(1)).findById(99);
		verify(stockProxy, never()).deleteStockByProductCode(anyString());
		verify(productRepository, never()).deleteById(anyInt());
	}

	@Test
	void deleteProduct_ShouldThrowRuntimeException_WhenStockDeletionFails() {
		when(productRepository.findById(1)).thenReturn(productModel);
		when(stockProxy.deleteStockByProductCode("NIKE-001"))
				.thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Stock delete failed."));

		RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.deleteProduct(1));

		assertEquals("Failed to delete stock. Product deletion aborted.", exception.getMessage());

		verify(productRepository, times(1)).findById(1);
		verify(stockProxy, times(1)).deleteStockByProductCode("NIKE-001");
		verify(productRepository, never()).deleteById(anyInt());
	}
}
