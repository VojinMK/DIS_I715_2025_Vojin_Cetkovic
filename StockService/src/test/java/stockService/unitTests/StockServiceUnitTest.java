package stockService.unitTests;

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

import serviceLibrary.dtos.StockDto;
import stockService.StockModel;
import stockService.StockRepository;
import stockService.StockServiceImpl;
import util.exceptions.ConflictException;
import util.exceptions.DataIntegrityViolationException;
import util.exceptions.NoDataFoundException;

@ExtendWith(MockitoExtension.class)
public class StockServiceUnitTest {
	@Mock
	private StockRepository stockRepository;

	@InjectMocks
	private StockServiceImpl stockService;

	private StockModel stockModel;

	@BeforeEach
	void setUp() {
		stockModel = new StockModel(1, "NIKE-001", 10);
	}

	@Test
	void getAllStock_ShouldReturnStockList_WhenStockExists() {
		when(stockRepository.findAll()).thenReturn(List.of(stockModel));

		ResponseEntity<?> response = stockService.getAllStock();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		@SuppressWarnings("unchecked")
		List<StockDto> stockList = (List<StockDto>) response.getBody();

		assertEquals(1, stockList.size());
		assertEquals(1, stockList.get(0).getId());
		assertEquals("NIKE-001", stockList.get(0).getProductCode());
		assertEquals(10, stockList.get(0).getQuantity());

		verify(stockRepository, times(1)).findAll();
	}

	@Test
	void getAllStock_ShouldReturnNotFound_WhenThereIsNoStock() {
		when(stockRepository.findAll()).thenReturn(List.of());

		ResponseEntity<?> response = stockService.getAllStock();

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals("There is no products.", response.getBody());

		verify(stockRepository, times(1)).findAll();
	}

	@Test
	void getStockByProductCode_ShouldReturnStock_WhenStockExists() {
		when(stockRepository.findByProductCode("NIKE-001")).thenReturn(stockModel);

		ResponseEntity<?> response = stockService.getStockByProductCode("NIKE-001");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		StockDto dto = (StockDto) response.getBody();

		assertEquals(1, dto.getId());
		assertEquals("NIKE-001", dto.getProductCode());
		assertEquals(10, dto.getQuantity());

		verify(stockRepository, times(1)).findByProductCode("NIKE-001");
	}

	@Test
	void getStockByProductCode_ShouldThrowNoDataFoundException_WhenStockDoesNotExist() {
		when(stockRepository.findByProductCode("MISSING-001")).thenReturn(null);

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> stockService.getStockByProductCode("MISSING-001"));

		assertEquals("Stock for product code MISSING-001 does not exist.", exception.getMessage());

		verify(stockRepository, times(1)).findByProductCode("MISSING-001");
	}

	@Test
	void initializeStock_ShouldCreateStock_WhenProductCodeDoesNotExist() {
		StockDto request = new StockDto("NIKE-001", 0);

		StockModel savedStock = new StockModel(1, "NIKE-001", 0);

		when(stockRepository.existsByProductCode("NIKE-001")).thenReturn(false);
		when(stockRepository.save(any(StockModel.class))).thenReturn(savedStock);

		ResponseEntity<?> response = stockService.initializeStock(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());

		StockDto dto = (StockDto) response.getBody();

		assertEquals(1, dto.getId());
		assertEquals("NIKE-001", dto.getProductCode());
		assertEquals(0, dto.getQuantity());

		verify(stockRepository, times(1)).existsByProductCode("NIKE-001");
		verify(stockRepository, times(1)).save(any(StockModel.class));
	}

	@Test
	void initializeStock_ShouldThrowConflictException_WhenProductCodeAlreadyExists() {
		StockDto request = new StockDto("NIKE-001", 0);

		when(stockRepository.existsByProductCode("NIKE-001")).thenReturn(true);

		ConflictException exception = assertThrows(ConflictException.class,
				() -> stockService.initializeStock(request));

		assertEquals("Stock for product code NIKE-001 already exists.", exception.getMessage());

		verify(stockRepository, times(1)).existsByProductCode("NIKE-001");
		verify(stockRepository, never()).save(any(StockModel.class));
	}

	@Test
	void updateStock_ShouldUpdateQuantity_WhenStockExistsAndRequestIsFromAdmin() {
		StockDto request = new StockDto("NIKE-001", 20);

		StockModel updatedStock = new StockModel(1, "NIKE-001", 20);

		when(stockRepository.findByProductCode("NIKE-001")).thenReturn(stockModel).thenReturn(updatedStock);

		ResponseEntity<?> response = stockService.updateStock(request, false);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		StockDto dto = (StockDto) response.getBody();

		assertEquals(1, dto.getId());
		assertEquals("NIKE-001", dto.getProductCode());
		assertEquals(20, dto.getQuantity());

		verify(stockRepository, times(2)).findByProductCode("NIKE-001");
		verify(stockRepository, times(1)).updateStock("NIKE-001", 20);
	}

	@Test
	void updateStock_ShouldKeepOldQuantity_WhenQuantityIsZeroAndRequestIsNotFromOrder() {
		StockDto request = new StockDto("NIKE-001", 0);

		when(stockRepository.findByProductCode("NIKE-001")).thenReturn(stockModel).thenReturn(stockModel);

		ResponseEntity<?> response = stockService.updateStock(request, false);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		StockDto dto = (StockDto) response.getBody();

		assertEquals(1, dto.getId());
		assertEquals("NIKE-001", dto.getProductCode());
		assertEquals(10, dto.getQuantity());

		verify(stockRepository, times(2)).findByProductCode("NIKE-001");
		verify(stockRepository, times(1)).updateStock("NIKE-001", 10);
	}

	@Test
	void updateStock_ShouldAllowZeroQuantity_WhenRequestIsFromOrder() {
		StockDto request = new StockDto("NIKE-001", 0);

		StockModel updatedStock = new StockModel(1, "NIKE-001", 0);

		when(stockRepository.findByProductCode("NIKE-001")).thenReturn(stockModel).thenReturn(updatedStock);

		ResponseEntity<?> response = stockService.updateStock(request, true);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		StockDto dto = (StockDto) response.getBody();

		assertEquals(1, dto.getId());
		assertEquals("NIKE-001", dto.getProductCode());
		assertEquals(0, dto.getQuantity());

		verify(stockRepository, times(2)).findByProductCode("NIKE-001");
		verify(stockRepository, times(1)).updateStock("NIKE-001", 0);
	}

	@Test
	void updateStock_ShouldThrowNoDataFoundException_WhenStockDoesNotExist() {
		StockDto request = new StockDto("MISSING-001", 5);

		when(stockRepository.findByProductCode("MISSING-001")).thenReturn(null);

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> stockService.updateStock(request, false));

		assertEquals("Stock for product code MISSING-001 does not exist.", exception.getMessage());

		verify(stockRepository, times(1)).findByProductCode("MISSING-001");
		verify(stockRepository, never()).updateStock(anyString(), anyInt());
	}

	@Test
	void updateStock_ShouldThrowDataIntegrityViolationException_WhenQuantityIsNegative() {
		StockDto request = new StockDto("NIKE-001", -1);

		when(stockRepository.findByProductCode("NIKE-001")).thenReturn(stockModel);

		DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
				() -> stockService.updateStock(request, false));

		assertEquals("Quantity can't be negative.", exception.getMessage());

		verify(stockRepository, times(1)).findByProductCode("NIKE-001");
		verify(stockRepository, never()).updateStock(anyString(), anyInt());
	}

	@Test
	void deleteStockByProductCode_ShouldDeleteStock_WhenStockExists() {
		when(stockRepository.findByProductCode("NIKE-001")).thenReturn(stockModel);

		ResponseEntity<?> response = stockService.deleteStockByProductCode("NIKE-001");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Stock for product code NIKE-001 successfully deleted.", response.getBody());

		verify(stockRepository, times(1)).findByProductCode("NIKE-001");
		verify(stockRepository, times(1)).delete(stockModel);
	}

	@Test
	void deleteStockByProductCode_ShouldThrowNoDataFoundException_WhenStockDoesNotExist() {
		when(stockRepository.findByProductCode("MISSING-001")).thenReturn(null);

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> stockService.deleteStockByProductCode("MISSING-001"));

		assertEquals("Stock for product code MISSING-001 does not exist.", exception.getMessage());

		verify(stockRepository, times(1)).findByProductCode("MISSING-001");
		verify(stockRepository, never()).delete(any(StockModel.class));
	}
}
