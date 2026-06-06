package orderService;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;

public interface OrderRepository extends JpaRepository<OrderModel, Integer> {

    List<OrderModel> findByUserEmail(String userEmail);
    
    OrderModel findById(int id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update OrderModel o set o.productCode = ?2, o.quantity = ?3, o.totalPrice = ?4, o.status = ?5 where o.id = ?1")
    void updateOrder(int id, String productCode, int quantity, double totalPrice, String status);
}