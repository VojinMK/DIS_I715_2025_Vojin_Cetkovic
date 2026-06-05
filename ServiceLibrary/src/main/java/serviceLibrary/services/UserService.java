package serviceLibrary.services;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import serviceLibrary.dtos.UserDto;

public interface UserService {

    @GetMapping("/users")
    ResponseEntity<?> getAllUsers();

    @GetMapping("/user/id")
    ResponseEntity<?> getUserById(@RequestParam Long id);

    @GetMapping("/user/email")
    ResponseEntity<?> getUserByEmail(@RequestParam String email);

    @PostMapping("/user/newUser")
    UserDto createUser(@RequestBody UserDto userDto);

    @PostMapping("/user/newAdmin")
    UserDto createAdmin(@RequestBody UserDto userDto);

    @PutMapping("/user")
    UserDto updateUser(@RequestBody UserDto userDto, @RequestHeader("X-User-Role") String role);

    @DeleteMapping("/user")
    ResponseEntity<?> deleteUser(@RequestParam Long id);
}