package productService;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface ProductRepository extends JpaRepository<ProductModel, Integer> {

    ProductModel findByProductCode(String productCode);
    
    ProductModel findById(int id);

    boolean existsByProductCode(String productCode);
    
    boolean existsById(int id);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update ProductModel p set p.name = ?2, p.brand = ?3, p.category = ?4, p.price = ?5 where p.productCode = ?1")
    void updateProduct(String productCode, String name, String brand, String category, double price);
}