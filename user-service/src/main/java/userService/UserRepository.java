package userService;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;


public interface UserRepository extends JpaRepository<UserModel, Long> {

    UserModel findByEmail(String email);
    
    UserModel findById(int id);

    boolean existsByEmail(String email);

    void deleteByEmail(String email);
    
    @Modifying
    @Transactional
    @Query("update UserModel u set u.firstName=?2, u.lastName=?3, u.password=?4, u.role=?5 where u.email=?1")
    void updateUser(String email, String firstName, String lastName, String password, String role);
    
    boolean existsByRole(String role);
}