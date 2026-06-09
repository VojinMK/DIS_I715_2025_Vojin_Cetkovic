package productService.integrationTests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import productService.ProductModel;
import productService.ProductRepository;
import serviceLibrary.dtos.StockDto;
import serviceLibrary.proxies.StockProxy;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductServiceIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ProductRepository productRepository;

	@MockitoBean
	private StockProxy stockProxy;

	@BeforeEach
	void setUp() {
		productRepository.deleteAll();
	}

	@Test
	void addProduct_ShouldCreateProductAndInitializeStock_WhenProductCodeDoesNotExist() throws Exception {
		when(stockProxy.initializeStock(any(StockDto.class)))
				.thenReturn((ResponseEntity) ResponseEntity.status(HttpStatus.CREATED).body("Stock initialized."));

		String json = """
				{
				    "productCode": "NIKE-001",
				    "name": "Nike Air Max",
				    "brand": "Nike",
				    "category": "FOOTWEAR",
				    "price": 12999.99
				}
				""";

		mockMvc.perform(post("/product").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.productCode").value("NIKE-001"))
				.andExpect(jsonPath("$.name").value("Nike Air Max")).andExpect(jsonPath("$.brand").value("Nike"))
				.andExpect(jsonPath("$.category").value("FOOTWEAR")).andExpect(jsonPath("$.price").value(12999.99));
	}

	@Test
	void addProduct_ShouldReturnConflict_WhenProductCodeAlreadyExists() throws Exception {
		productRepository.save(new ProductModel("NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99));

		String json = """
				{
				    "productCode": "NIKE-001",
				    "name": "Nike Air Max 2",
				    "brand": "Nike",
				    "category": "FOOTWEAR",
				    "price": 13999.99
				}
				""";

		mockMvc.perform(post("/product").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void getAllProducts_ShouldReturnProducts_WhenProductsExist() throws Exception {
		productRepository.save(new ProductModel("NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99));

		mockMvc.perform(get("/products")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].productCode").value("NIKE-001"))
				.andExpect(jsonPath("$[0].name").value("Nike Air Max")).andExpect(jsonPath("$[0].brand").value("Nike"))
				.andExpect(jsonPath("$[0].category").value("FOOTWEAR"))
				.andExpect(jsonPath("$[0].price").value(12999.99));
	}

	@Test
	void getAllProducts_ShouldReturnNotFound_WhenNoProductsExist() throws Exception {
		mockMvc.perform(get("/products")).andExpect(status().isNotFound())
				.andExpect(content().string("There is no products."));
	}

	@Test
	void getProductByCode_ShouldReturnProduct_WhenProductExists() throws Exception {
		productRepository.save(new ProductModel("NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99));

		mockMvc.perform(get("/product/code").param("productCode", "NIKE-001")).andExpect(status().isOk())
				.andExpect(jsonPath("$.productCode").value("NIKE-001"))
				.andExpect(jsonPath("$.name").value("Nike Air Max")).andExpect(jsonPath("$.brand").value("Nike"))
				.andExpect(jsonPath("$.category").value("FOOTWEAR")).andExpect(jsonPath("$.price").value(12999.99));
	}

	@Test
	void getProductById_ShouldReturnProduct_WhenProductExists() throws Exception {
		ProductModel savedProduct = productRepository
				.save(new ProductModel("NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99));

		mockMvc.perform(get("/product/id").param("id", String.valueOf(savedProduct.getId()))).andExpect(status().isOk())
				.andExpect(jsonPath("$.productCode").value("NIKE-001"))
				.andExpect(jsonPath("$.name").value("Nike Air Max"));
	}

	@Test
	void updateProduct_ShouldUpdateOnlyProvidedFields_WhenProductExists() throws Exception {
		productRepository.save(new ProductModel("NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99));

		String json = """
				{
				    "productCode": "NIKE-001",
				    "name": "Nike Air Max Updated",
				    "price": 13999.99
				}
				""";

		mockMvc.perform(put("/product").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.productCode").value("NIKE-001"))
				.andExpect(jsonPath("$.name").value("Nike Air Max Updated"))
				.andExpect(jsonPath("$.brand").value("Nike")).andExpect(jsonPath("$.category").value("FOOTWEAR"))
				.andExpect(jsonPath("$.price").value(13999.99));
	}

	@Test
	void updateProduct_ShouldReturnError_WhenProductDoesNotExist() throws Exception {
		String json = """
				{
				    "productCode": "MISSING-001",
				    "name": "Missing Product"
				}
				""";

		mockMvc.perform(put("/product").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void deleteProduct_ShouldDeleteProductAndStock_WhenProductExists() throws Exception {
		ProductModel savedProduct = productRepository
				.save(new ProductModel("NIKE-001", "Nike Air Max", "Nike", "FOOTWEAR", 12999.99));

		when(stockProxy.deleteStockByProductCode("NIKE-001")).thenReturn(ResponseEntity.ok("Stock deleted."));

		mockMvc.perform(delete("/product").param("id", String.valueOf(savedProduct.getId()))).andExpect(status().isOk())
				.andExpect(content().string("Product and stock successfully deleted."));
	}

	@Test
	void deleteProduct_ShouldReturnError_WhenProductDoesNotExist() throws Exception {
		mockMvc.perform(delete("/product").param("id", "99")).andExpect(status().is4xxClientError());
	}
}
