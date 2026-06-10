package serviceLibrary.proxies;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import serviceLibrary.dtos.UserDto;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("user-service")
public interface UserProxy {

	@GetMapping("/user/id")
	ResponseEntity<?> getUserById(@RequestParam(value = "id") Long id);

	@GetMapping("/user/email")
	ResponseEntity<UserDto> getUserByEmail(@RequestParam(value = "email") String email);
}
