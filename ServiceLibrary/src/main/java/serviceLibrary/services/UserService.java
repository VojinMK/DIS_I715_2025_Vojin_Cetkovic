package serviceLibrary.services;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import serviceLibrary.dtos.UserDto;

public interface UserService {

    @GetMapping("/users")
    List<UserDto> getAllUsers();

    @GetMapping("/user/{id}")
    UserDto getUserById(@PathVariable Long id);

    @GetMapping("/user/email")
    UserDto getUserByEmail(@RequestParam String email);

    @PostMapping("/user/newUser")
    UserDto createUser(@RequestBody UserDto userDto);

    @PostMapping("/user/newAdmin")
    UserDto createAdmin(@RequestBody UserDto userDto);

    @PutMapping("/users")
    UserDto updateUser(@RequestBody UserDto userDto);

    @DeleteMapping("/user/{id}")
    void deleteUser(@PathVariable Long id);
}