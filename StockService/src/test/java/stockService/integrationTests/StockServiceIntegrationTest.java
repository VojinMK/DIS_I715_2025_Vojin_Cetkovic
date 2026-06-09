package stockService.integrationTests;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import stockService.StockModel;
import stockService.StockRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StockServiceIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StockRepository stockRepository;

	@BeforeEach
	void setUp() {
		stockRepository.deleteAll();
	}

	@Test
	void initializeStock_ShouldCreateStock_WhenProductCodeDoesNotExist() throws Exception {
		String json = """
				{
				    "productCode": "NIKE-001",
				    "quantity": 0
				}
				""";

		mockMvc.perform(post("/stock").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.productCode").value("NIKE-001"))
				.andExpect(jsonPath("$.quantity").value(0));
	}

	@Test
	void initializeStock_ShouldReturnError_WhenProductCodeAlreadyExists() throws Exception {
		stockRepository.save(new StockModel("NIKE-001", 10));

		String json = """
				{
				    "productCode": "NIKE-001",
				    "quantity": 0
				}
				""";

		mockMvc.perform(post("/stock").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void getAllStock_ShouldReturnStockList_WhenStockExists() throws Exception {
		stockRepository.save(new StockModel("NIKE-001", 10));

		mockMvc.perform(get("/stocks")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].productCode").value("NIKE-001"))
				.andExpect(jsonPath("$[0].quantity").value(10));
	}

	@Test
	void getAllStock_ShouldReturnNotFound_WhenNoStockExists() throws Exception {
		mockMvc.perform(get("/stocks")).andExpect(status().isNotFound())
				.andExpect(content().string("There is no products."));
	}

	@Test
	void getStockByProductCode_ShouldReturnStock_WhenStockExists() throws Exception {
		stockRepository.save(new StockModel("NIKE-001", 10));

		mockMvc.perform(get("/stock").param("productCode", "NIKE-001")).andExpect(status().isOk())
				.andExpect(jsonPath("$.productCode").value("NIKE-001")).andExpect(jsonPath("$.quantity").value(10));
	}

	@Test
	void getStockByProductCode_ShouldReturnError_WhenStockDoesNotExist() throws Exception {
		mockMvc.perform(get("/stock").param("productCode", "MISSING-001")).andExpect(status().is4xxClientError());
	}

	@Test
	void updateStock_ShouldUpdateQuantity_WhenStockExistsAndFromOrderIsFalse() throws Exception {
		stockRepository.save(new StockModel("NIKE-001", 10));

		String json = """
				{
				    "productCode": "NIKE-001",
				    "quantity": 20
				}
				""";

		mockMvc.perform(put("/stock").param("fromOrder", "false").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.productCode").value("NIKE-001"))
				.andExpect(jsonPath("$.quantity").value(20));
	}

	@Test
	void updateStock_ShouldKeepOldQuantity_WhenQuantityIsZeroAndFromOrderIsFalse() throws Exception {
		stockRepository.save(new StockModel("NIKE-001", 10));

		String json = """
				{
				    "productCode": "NIKE-001",
				    "quantity": 0
				}
				""";

		mockMvc.perform(put("/stock").param("fromOrder", "false").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.productCode").value("NIKE-001"))
				.andExpect(jsonPath("$.quantity").value(10));
	}

	@Test
	void updateStock_ShouldAllowZeroQuantity_WhenFromOrderIsTrue() throws Exception {
		stockRepository.save(new StockModel("NIKE-001", 10));

		String json = """
				{
				    "productCode": "NIKE-001",
				    "quantity": 0
				}
				""";

		mockMvc.perform(put("/stock").param("fromOrder", "true").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.productCode").value("NIKE-001"))
				.andExpect(jsonPath("$.quantity").value(0));
	}

	@Test
	void updateStock_ShouldReturnError_WhenQuantityIsNegative() throws Exception {
		stockRepository.save(new StockModel("NIKE-001", 10));

		String json = """
				{
				    "productCode": "NIKE-001",
				    "quantity": -1
				}
				""";

		mockMvc.perform(put("/stock").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void updateStock_ShouldReturnError_WhenStockDoesNotExist() throws Exception {
		String json = """
				{
				    "productCode": "MISSING-001",
				    "quantity": 5
				}
				""";

		mockMvc.perform(put("/stock").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void deleteStockByProductCode_ShouldDeleteStock_WhenStockExists() throws Exception {
		stockRepository.save(new StockModel("NIKE-001", 10));

		mockMvc.perform(delete("/stock").param("productCode", "NIKE-001")).andExpect(status().isOk())
				.andExpect(content().string("Stock for product code NIKE-001 successfully deleted."));

		mockMvc.perform(get("/stock").param("productCode", "NIKE-001")).andExpect(status().is4xxClientError());
	}

	@Test
	void deleteStockByProductCode_ShouldReturnError_WhenStockDoesNotExist() throws Exception {
		mockMvc.perform(delete("/stock").param("productCode", "MISSING-001")).andExpect(status().is4xxClientError());
	}
}
