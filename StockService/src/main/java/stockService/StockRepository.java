package stockService;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface StockRepository extends JpaRepository<StockModel, Integer> {

    StockModel findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update StockModel s set s.quantity = ?2 where s.productCode = ?1")
    void updateStock(String productCode, int quantity);
}