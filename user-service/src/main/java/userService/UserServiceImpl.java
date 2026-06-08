package userService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import serviceLibrary.dtos.UserDto;
import serviceLibrary.services.UserService;
import util.exceptions.ConflictException;
import util.exceptions.DataIntegrityViolationException;
import util.exceptions.NoDataFoundException;

@RestController
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public ResponseEntity<?> getAllUsers() {
		List<UserModel> listOfModels = userRepository.findAll();
		ArrayList<UserDto> listOfDtos = new ArrayList<>();
		for (UserModel model : listOfModels) {
			listOfDtos.add(convertModelToDto(model));
		}
		if (listOfDtos.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no users.");
		}
		return ResponseEntity.ok(listOfDtos);
	}

	@Override
	public ResponseEntity<?> getUserById(Long id) {
		UserModel user = userRepository.findById(id.intValue());
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no user with this id.");
		}
		return ResponseEntity.status(HttpStatus.OK).body(convertModelToDto(user));
	}

	@Override
	public ResponseEntity<?> getUserByEmail(String email) {
		UserModel user = userRepository.findByEmail(email);

		if (user == null) {
			throw new NoDataFoundException("User with email " + email + " does not exist.");
		}

		return ResponseEntity.status(HttpStatus.OK).body(convertModelToDto(user));
	}

	@Override
	public UserDto createUser(UserDto userDto) {
		UserModel existingUser = userRepository.findByEmail(userDto.getEmail());

		if (existingUser != null) {
			throw new ConflictException("User with email " + userDto.getEmail() + " already exists.");
		}

		UserModel user = new UserModel(userDto.getFirstName(), userDto.getLastName(), userDto.getEmail(),
				userDto.getPassword(), "USER");

		UserModel savedUser = userRepository.save(user);

		return convertModelToDto(savedUser);
	}

	@Override
	public UserDto createAdmin(UserDto userDto) {
		UserModel existingUser = userRepository.findByEmail(userDto.getEmail());

		if (existingUser != null) {
			throw new ConflictException("User with email " + userDto.getEmail() + " already exists.");
		}

		UserModel admin = new UserModel(userDto.getFirstName(), userDto.getLastName(), userDto.getEmail(),
				userDto.getPassword(), "ADMIN");

		UserModel savedAdmin = userRepository.save(admin);

		return convertModelToDto(savedAdmin);
	}

	@Override
	public UserDto updateUser(UserDto userDto, @RequestHeader("X-User-Role") String role) {
		UserModel existingUser = userRepository.findByEmail(userDto.getEmail());
		Set<String> allowed_roles = Set.of("ADMIN", "USER");
		
		if (existingUser == null) {
			throw new NoDataFoundException("User with email " + userDto.getEmail() + " does not exist.");
		}
		if (userDto.getRole()!=null) {
			if (!allowed_roles.contains(userDto.getRole().toUpperCase())) {
				throw new DataIntegrityViolationException("That role doesn't exist.");
			} 
		}
		if(!role.equals("ROLE_ADMIN")) {
			throw new DataIntegrityViolationException("Only admin can update users.");
		}

		String firstName = userDto.getFirstName() != null ? userDto.getFirstName() : existingUser.getFirstName();

		String lastName = userDto.getLastName() != null ? userDto.getLastName() : existingUser.getLastName();

		String password = userDto.getPassword() != null ? userDto.getPassword() : existingUser.getPassword();

		String newRole = userDto.getRole() != null ? userDto.getRole().toUpperCase() : existingUser.getRole();
		
		userRepository.updateUser(userDto.getEmail(), firstName, lastName, password, newRole);

		UserModel updatedUser = userRepository.findByEmail(userDto.getEmail());

		return convertModelToDto(updatedUser);
	}

	@Override
	public ResponseEntity<?> deleteUser(Long id) {
		UserModel user = userRepository.findById(id.intValue());
		if (user == null) {
			throw new NoDataFoundException("User with id " + id + " does not exist.");
		}

		userRepository.delete(user);
		return ResponseEntity.ok("User with id " + id + " is deleted");
	}

	private UserDto convertModelToDto(UserModel user) {
		return new UserDto(Long.valueOf(user.getId()), user.getFirstName(), user.getLastName(), user.getEmail(),
				user.getPassword(), user.getRole());
	}

	private UserModel convertDtoToModel(UserDto user) {
		return new UserModel(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword(),
				user.getRole());
	}
}