package productService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import serviceLibrary.dtos.ProductDto;
import serviceLibrary.services.ProductService;
import util.exceptions.ConflictException;
import util.exceptions.NoDataFoundException;
import serviceLibrary.dtos.StockDto;
import serviceLibrary.proxies.StockProxy;

@RestController
public class ProductServiceImpl implements ProductService {

	@Autowired
    private  ProductRepository productRepository;
	
	@Autowired
	private  StockProxy stockProxy;

    @Override
    public ResponseEntity<?> getAllProducts() {
        List<ProductModel> productModels = productRepository.findAll();
        List<ProductDto> productDtos = new ArrayList<>();

        for (ProductModel model : productModels) {
        	productDtos.add(convertModelToDto(model));
        }
        if (productDtos.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no products.");
		}

        return ResponseEntity.ok(productDtos);
    }

    @Override
    public ResponseEntity<?> getProductById(int id) {
        ProductModel product = productRepository.findById(id);
		if (product == null) {
			throw new NoDataFoundException("Product with id " + id + " not found.");
		}
		return ResponseEntity.status(HttpStatus.OK).body(convertModelToDto(product));
    }

    @Override
    public ResponseEntity<?> getProductByCode(String productCode) {
        ProductModel product = productRepository.findByProductCode(productCode);

        if (product == null) {
            throw new NoDataFoundException("Product with code " + productCode + " not found.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(convertModelToDto(product));
    }

    @Override
    public ResponseEntity<?> addProduct(ProductDto dto) {
        if (productRepository.existsByProductCode(dto.getProductCode())) {
            throw new ConflictException("Product with code " + dto.getProductCode() + " already exists.");
        }

        ProductModel model = convertDtoToModel(dto);
        ProductModel savedProduct = productRepository.save(model);

        StockDto stockDto = new StockDto(savedProduct.getProductCode(), 0);
        ResponseEntity<?> stockResponse = stockProxy.initializeStock(stockDto);

        if (!stockResponse.getStatusCode().is2xxSuccessful()) {
            productRepository.delete(savedProduct);
            throw new RuntimeException("Product created, but failed to initialize stock. Product deleted.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(convertModelToDto(savedProduct));
    }

    @Override
    public ResponseEntity<?> updateProduct(ProductDto dto) {
    	
    	ProductModel existingProduct=productRepository.findByProductCode(dto.getProductCode());
    	
        if (existingProduct==null) {
            throw new NoDataFoundException("Product with code " + dto.getProductCode() + " does not exist.");
        }

        String name = dto.getName() != null
                ? dto.getName()
                : existingProduct.getName();

        String brand = dto.getBrand() != null
                ? dto.getBrand()
                : existingProduct.getBrand();

        String category = dto.getCategory() != null
                ? dto.getCategory()
                : existingProduct.getCategory();

        double price = dto.getPrice() != 0.0
                ? dto.getPrice()
                : existingProduct.getPrice();

        productRepository.updateProduct(
                dto.getProductCode(),
                name,
                brand,
                category,
                price
        );

        ProductModel updatedProduct = productRepository.findByProductCode(dto.getProductCode());

        return ResponseEntity.ok(convertModelToDto(updatedProduct));
    }

    @Override
    public ResponseEntity<?> deleteProduct(int id) {
    	ProductModel product = productRepository.findById(id);
        if (product==null) {
            throw new NoDataFoundException("Product not found.");
        }
        
        ResponseEntity<String> stockResponse = stockProxy.deleteStockByProductCode(product.getProductCode());

        if (!stockResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to delete stock. Product deletion aborted.");
        }

        productRepository.deleteById(id);
        
        return ResponseEntity.ok("Product and stock successfully deleted.");
    }

    private ProductDto convertModelToDto(ProductModel model) {
        return new ProductDto(
        		model.getId(),
                model.getProductCode(),
                model.getName(),
                model.getBrand(),
                model.getCategory(),
                model.getPrice()
        );
    }

    private ProductModel convertDtoToModel(ProductDto dto) {
        return new ProductModel(
                dto.getProductCode(),
                dto.getName(),
                dto.getBrand(),
                dto.getCategory(),
                dto.getPrice()
        );
    }
}