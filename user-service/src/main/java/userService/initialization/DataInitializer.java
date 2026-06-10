package userService.initialization;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import userService.UserModel;
import userService.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createInitialAdmin(UserRepository userRepository) {
        return args -> {

            if (!userRepository.existsByEmail("admin@gmail.com")) {

                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

                UserModel admin = new UserModel();
                admin.setFirstName("Admin");
                admin.setLastName("Admin");
                admin.setEmail("admin@gmail.com");
                admin.setPassword(encoder.encode("admin"));
                admin.setRole("ADMIN");

                userRepository.save(admin);

                System.out.println("Initial admin created: admin@gmail.com / admin");
            } else {
                System.out.println("Initial admin already exists.");
            }
        };
    }
}