package userService.unitTests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import serviceLibrary.dtos.UserDto;
import userService.UserModel;
import userService.UserRepository;
import userService.UserServiceImpl;
import util.exceptions.ConflictException;
import util.exceptions.DataIntegrityViolationException;
import util.exceptions.NoDataFoundException;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserServiceImpl userService;

	private UserModel adminModel;
	private UserModel userModel;

	@BeforeEach
	void setUp() {
		adminModel = new UserModel(1L, "Admin", "Admin", "admin@gmail.com", "hashedAdminPassword", "ADMIN");

		userModel = new UserModel(2L, "User", "User", "user@gmail.com", "hashedUserPassword", "USER");
	}

	@Test
	void getAllUsers_ShouldReturnListOfUsers_WhenUsersExist() {
		when(userRepository.findAll()).thenReturn(List.of(adminModel, userModel));

		ResponseEntity<?> response = userService.getAllUsers();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		@SuppressWarnings("unchecked")
		List<UserDto> users = (List<UserDto>) response.getBody();

		assertEquals(2, users.size());
		assertEquals("admin@gmail.com", users.get(0).getEmail());
		assertEquals("user@gmail.com", users.get(1).getEmail());

		verify(userRepository, times(1)).findAll();
	}

	@Test
	void getAllUsers_ShouldReturnNotFound_WhenThereAreNoUsers() {
		when(userRepository.findAll()).thenReturn(List.of());

		ResponseEntity<?> response = userService.getAllUsers();

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals("There is no users.", response.getBody());

		verify(userRepository, times(1)).findAll();
	}

	@Test
	void getUserById_ShouldReturnUser_WhenUserExists() {
		when(userRepository.findById(2)).thenReturn(userModel);

		ResponseEntity<?> response = userService.getUserById(2L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		UserDto dto = (UserDto) response.getBody();

		assertEquals(2L, dto.getId());
		assertEquals("User", dto.getFirstName());
		assertEquals("User", dto.getLastName());
		assertEquals("user@gmail.com", dto.getEmail());
		assertEquals("USER", dto.getRole());

		verify(userRepository, times(1)).findById(2);
	}

	@Test
	void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() {
		when(userRepository.findById(99)).thenReturn(null);

		ResponseEntity<?> response = userService.getUserById(99L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals("There is no user with this id.", response.getBody());

		verify(userRepository, times(1)).findById(99);
	}

	@Test
	void getUserByEmail_ShouldReturnUser_WhenUserExists() {
		when(userRepository.findByEmail("user@gmail.com")).thenReturn(userModel);

		ResponseEntity<?> response = userService.getUserByEmail("user@gmail.com");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		UserDto dto = (UserDto) response.getBody();

		assertEquals(2L, dto.getId());
		assertEquals("User", dto.getFirstName());
		assertEquals("User", dto.getLastName());
		assertEquals("user@gmail.com", dto.getEmail());
		assertEquals("USER", dto.getRole());

		verify(userRepository, times(1)).findByEmail("user@gmail.com");
	}

	@Test
	void getUserByEmail_ShouldThrowNoDataFoundException_WhenUserDoesNotExist() {
		when(userRepository.findByEmail("missing@gmail.com")).thenReturn(null);

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> userService.getUserByEmail("missing@gmail.com"));

		assertEquals("User with email missing@gmail.com does not exist.", exception.getMessage());

		verify(userRepository, times(1)).findByEmail("missing@gmail.com");
	}

	@Test
	void createUser_ShouldCreateUser_WhenEmailDoesNotExist() {
		UserDto request = new UserDto(null, "User", "User", "user@gmail.com", "hashedPassword", null);

		UserModel savedUser = new UserModel(2L, "User", "User", "user@gmail.com", "hashedPassword", "USER");

		when(userRepository.findByEmail("user@gmail.com")).thenReturn(null);
		when(userRepository.save(any(UserModel.class))).thenReturn(savedUser);

		UserDto result = userService.createUser(request);

		assertNotNull(result);
		assertEquals(2L, result.getId());
		assertEquals("User", result.getFirstName());
		assertEquals("User", result.getLastName());
		assertEquals("user@gmail.com", result.getEmail());
		assertEquals("hashedPassword", result.getPassword());
		assertEquals("USER", result.getRole());

		verify(userRepository, times(1)).findByEmail("user@gmail.com");
		verify(userRepository, times(1)).save(any(UserModel.class));
	}

	@Test
	void createUser_ShouldThrowConflictException_WhenEmailAlreadyExists() {
		UserDto request = new UserDto(null, "User", "User", "user@gmail.com", "password", null);

		when(userRepository.findByEmail("user@gmail.com")).thenReturn(userModel);

		ConflictException exception = assertThrows(ConflictException.class, () -> userService.createUser(request));

		assertEquals("User with email user@gmail.com already exists.", exception.getMessage());

		verify(userRepository, times(1)).findByEmail("user@gmail.com");
		verify(userRepository, never()).save(any(UserModel.class));
	}

	@Test
	void createAdmin_ShouldCreateAdmin_WhenEmailDoesNotExist() {
		UserDto request = new UserDto(null, "Admin", "Admin", "admin@gmail.com", "hashedPassword", null);

		UserModel savedAdmin = new UserModel(1L, "Admin", "Admin", "admin@gmail.com", "hashedPassword", "ADMIN");

		when(userRepository.findByEmail("admin@gmail.com")).thenReturn(null);
		when(userRepository.save(any(UserModel.class))).thenReturn(savedAdmin);

		UserDto result = userService.createAdmin(request);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("Admin", result.getFirstName());
		assertEquals("Admin", result.getLastName());
		assertEquals("admin@gmail.com", result.getEmail());
		assertEquals("hashedPassword", result.getPassword());
		assertEquals("ADMIN", result.getRole());

		verify(userRepository, times(1)).findByEmail("admin@gmail.com");
		verify(userRepository, times(1)).save(any(UserModel.class));
	}

	@Test
	void createAdmin_ShouldThrowConflictException_WhenEmailAlreadyExists() {
		UserDto request = new UserDto(null, "Admin", "Admin", "admin@gmail.com", "password", null);

		when(userRepository.findByEmail("admin@gmail.com")).thenReturn(adminModel);

		ConflictException exception = assertThrows(ConflictException.class, () -> userService.createAdmin(request));

		assertEquals("User with email admin@gmail.com already exists.", exception.getMessage());

		verify(userRepository, times(1)).findByEmail("admin@gmail.com");
		verify(userRepository, never()).save(any(UserModel.class));
	}

	@Test
	void updateUser_ShouldUpdateOnlyProvidedFields_WhenAdminUpdatesUser() {
		UserDto request = new UserDto(null, "Updated", null, "user@gmail.com", null, "admin");

		UserModel updatedUser = new UserModel(2L, "Updated", "User", "user@gmail.com", "hashedUserPassword",
				"ADMIN");

		when(userRepository.findByEmail("user@gmail.com")).thenReturn(userModel).thenReturn(updatedUser);

		UserDto result = userService.updateUser(request, "ROLE_ADMIN");

		assertNotNull(result);
		assertEquals(2L, result.getId());
		assertEquals("Updated", result.getFirstName());
		assertEquals("User", result.getLastName());
		assertEquals("user@gmail.com", result.getEmail());
		assertEquals("hashedUserPassword", result.getPassword());
		assertEquals("ADMIN", result.getRole());

		verify(userRepository, times(2)).findByEmail("user@gmail.com");
		verify(userRepository, times(1)).updateUser("user@gmail.com", "Updated", "User",
				"hashedUserPassword", "ADMIN");
	}

	@Test
	void updateUser_ShouldKeepOldValues_WhenFieldsAreNull() {
		UserDto request = new UserDto(null, null, null, "user@gmail.com", null, null);

		when(userRepository.findByEmail("user@gmail.com")).thenReturn(userModel).thenReturn(userModel);

		UserDto result = userService.updateUser(request, "ROLE_ADMIN");

		assertNotNull(result);
		assertEquals(2L, result.getId());
		assertEquals("User", result.getFirstName());
		assertEquals("User", result.getLastName());
		assertEquals("user@gmail.com", result.getEmail());
		assertEquals("hashedUserPassword", result.getPassword());
		assertEquals("USER", result.getRole());

		verify(userRepository, times(2)).findByEmail("user@gmail.com");
		verify(userRepository, times(1)).updateUser("user@gmail.com", "User", "User", "hashedUserPassword",
				"USER");
	}

	@Test
	void updateUser_ShouldThrowNoDataFoundException_WhenUserDoesNotExist() {
		UserDto request = new UserDto(null, "Test", "Test", "missing@gmail.com", "password", "USER");

		when(userRepository.findByEmail("missing@gmail.com")).thenReturn(null);

		NoDataFoundException exception = assertThrows(NoDataFoundException.class,
				() -> userService.updateUser(request, "ROLE_ADMIN"));

		assertEquals("User with email missing@gmail.com does not exist.", exception.getMessage());

		verify(userRepository, times(1)).findByEmail("missing@gmail.com");
		verify(userRepository, never()).updateUser(anyString(), anyString(), anyString(), anyString(), anyString());
	}

	@Test
	void updateUser_ShouldThrowDataIntegrityViolationException_WhenRoleDoesNotExist() {
		UserDto request = new UserDto(null, null, null, "user@gmail.com", null, "MANAGER");

		when(userRepository.findByEmail("user@gmail.com")).thenReturn(userModel);

		DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
				() -> userService.updateUser(request, "ROLE_ADMIN"));

		assertEquals("That role doesn't exist.", exception.getMessage());

		verify(userRepository, times(1)).findByEmail("user@gmail.com");
		verify(userRepository, never()).updateUser(anyString(), anyString(), anyString(), anyString(), anyString());
	}

	@Test
	void updateUser_ShouldThrowDataIntegrityViolationException_WhenRoleHeaderIsNotAdmin() {
		UserDto request = new UserDto(null, "Updated", null, "user@gmail.com", null, "USER");

		when(userRepository.findByEmail("user@gmail.com")).thenReturn(userModel);

		DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
				() -> userService.updateUser(request, "ROLE_USER"));

		assertEquals("Only admin can update users.", exception.getMessage());

		verify(userRepository, times(1)).findByEmail("user@gmail.com");
		verify(userRepository, never()).updateUser(anyString(), anyString(), anyString(), anyString(), anyString());
	}

	@Test
	void deleteUser_ShouldDeleteUser_WhenUserExists() {
		when(userRepository.findById(2)).thenReturn(userModel);

		ResponseEntity<?> response = userService.deleteUser(2L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("User with id 2 is deleted", response.getBody());

		verify(userRepository, times(1)).findById(2);
		verify(userRepository, times(1)).delete(userModel);
	}

	@Test
	void deleteUser_ShouldThrowNoDataFoundException_WhenUserDoesNotExist() {
		when(userRepository.findById(99)).thenReturn(null);

		NoDataFoundException exception = assertThrows(NoDataFoundException.class, () -> userService.deleteUser(99L));

		assertEquals("User with id 99 does not exist.", exception.getMessage());

		verify(userRepository, times(1)).findById(99);
		verify(userRepository, never()).delete(any(UserModel.class));
	}
}
