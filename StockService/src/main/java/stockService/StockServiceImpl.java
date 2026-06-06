package stockService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import serviceLibrary.dtos.StockDto;
import serviceLibrary.services.StockService;
import util.exceptions.ConflictException;
import util.exceptions.DataIntegrityViolationException;
import util.exceptions.NoDataFoundException;

@RestController
public class StockServiceImpl implements StockService {

	@Autowired
	private StockRepository stockRepository;

	@Override
	public ResponseEntity<?> getAllStock() {
		List<StockModel> stockModels = stockRepository.findAll();
		List<StockDto> StockDtos = new ArrayList<>();

		for (StockModel model : stockModels) {
			StockDtos.add(convertModelToDto(model));
		}

		if (StockDtos.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no products.");
		}

		return ResponseEntity.ok(StockDtos);
	}

	@Override
	public ResponseEntity<?> getStockByProductCode(String productCode) {
		StockModel stock = stockRepository.findByProductCode(productCode);

		if (stock == null) {
			throw new NoDataFoundException("Stock for product code " + productCode + " does not exist.");
		}

		return ResponseEntity.ok(convertModelToDto(stock));
	}

	@Override
	public ResponseEntity<?> initializeStock(StockDto dto) {
		if (stockRepository.existsByProductCode(dto.getProductCode())) {
			throw new ConflictException("Stock for product code " + dto.getProductCode() + " already exists.");
		}

		StockModel model = new StockModel(dto.getProductCode(), dto.getQuantity());
		StockModel savedStock = stockRepository.save(model);

		return ResponseEntity.status(HttpStatus.CREATED).body(convertModelToDto(savedStock));
	}

	@Override
	public ResponseEntity<?> updateStock(StockDto dto, boolean fromOrder) {
		StockModel existingStock = stockRepository.findByProductCode(dto.getProductCode());
		int quantity;

		if (existingStock == null) {
			throw new NoDataFoundException("Stock for product code " + dto.getProductCode() + " does not exist.");
		}
		if (dto.getQuantity() < 0) {
			throw new DataIntegrityViolationException("Quantity can't be negative.");
		}
		if (fromOrder == true) {
			quantity = dto.getQuantity();

		}else {
			quantity = dto.getQuantity() != 0 ? dto.getQuantity() : existingStock.getQuantity();
		}
		stockRepository.updateStock(dto.getProductCode(), quantity);

		StockModel updatedProduct = stockRepository.findByProductCode(dto.getProductCode());

		return ResponseEntity.ok(convertModelToDto(updatedProduct));
	}

	@Override
	public ResponseEntity<?> deleteStockByProductCode(String productCode) {
		StockModel stock = stockRepository.findByProductCode(productCode);

		if (stock == null) {
			throw new NoDataFoundException("Stock for product code " + productCode + " does not exist.");
		}

		stockRepository.delete(stock);

		return ResponseEntity.ok("Stock for product code " + productCode + " successfully deleted.");
	}

	private StockDto convertModelToDto(StockModel model) {
		return new StockDto(model.getId(), model.getProductCode(), model.getQuantity());
	}
}