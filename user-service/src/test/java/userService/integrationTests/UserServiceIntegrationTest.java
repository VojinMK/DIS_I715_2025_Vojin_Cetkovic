package userService.integrationTests;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import userService.UserModel;
import userService.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserServiceIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
	}

	@Test
	void createAdmin_ShouldCreateAdmin_WhenEmailDoesNotExist() throws Exception {
		String json = """
				{
				    "firstName": "Admin",
				    "lastName": "Admin",
				    "email": "admin@gmail.com",
				    "password": "admin"
				}
				""";

		mockMvc.perform(post("/user/newAdmin").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.firstName").value("Admin"))
				.andExpect(jsonPath("$.lastName").value("Admin"))
				.andExpect(jsonPath("$.email").value("admin@gmail.com"))
				.andExpect(jsonPath("$.password").value("admin")).andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	void createUser_ShouldCreateUser_WhenEmailDoesNotExist() throws Exception {
		String json = """
				{
				    "firstName": "User",
				    "lastName": "User",
				    "email": "user@gmail.com",
				    "password": "user"
				}
				""";

		mockMvc.perform(post("/user/newUser").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.firstName").value("User"))
				.andExpect(jsonPath("$.lastName").value("User")).andExpect(jsonPath("$.email").value("user@gmail.com"))
				.andExpect(jsonPath("$.password").value("user")).andExpect(jsonPath("$.role").value("USER"));
	}

	@Test
	void getAllUsers_ShouldReturnUsers_WhenUsersExist() throws Exception {
		userRepository.save(new UserModel("User", "User", "user@gmail.com", "user", "USER"));

		mockMvc.perform(get("/users")).andExpect(status().isOk()).andExpect(jsonPath("$[0].firstName").value("User"))
				.andExpect(jsonPath("$[0].lastName").value("User"))
				.andExpect(jsonPath("$[0].email").value("user@gmail.com"))
				.andExpect(jsonPath("$[0].role").value("USER"));
	}

	@Test
	void getAllUsers_ShouldReturnNotFound_WhenNoUsersExist() throws Exception {
		mockMvc.perform(get("/users")).andExpect(status().isNotFound())
				.andExpect(content().string("There is no users."));
	}

	@Test
	void getUserByEmail_ShouldReturnUser_WhenUserExists() throws Exception {
		userRepository.save(new UserModel("User", "User", "user@gmail.com", "user", "USER"));

		mockMvc.perform(get("/user/email").param("email", "user@gmail.com")).andExpect(status().isOk())
				.andExpect(jsonPath("$.firstName").value("User")).andExpect(jsonPath("$.lastName").value("User"))
				.andExpect(jsonPath("$.email").value("user@gmail.com")).andExpect(jsonPath("$.role").value("USER"));
	}

	@Test
	void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
		UserModel savedUser = userRepository.save(new UserModel("User", "User", "user@gmail.com", "user", "USER"));

		mockMvc.perform(get("/user/id").param("id", savedUser.getId().toString())).andExpect(status().isOk())
				.andExpect(jsonPath("$.firstName").value("User")).andExpect(jsonPath("$.lastName").value("User"))
				.andExpect(jsonPath("$.email").value("user@gmail.com")).andExpect(jsonPath("$.role").value("USER"));
	}

	@Test
	void updateUser_ShouldUpdateUser_WhenUserExistsAndHeaderRoleIsAdmin() throws Exception {
		userRepository.save(new UserModel("User", "User", "user@gmail.com", "oldPassword", "USER"));

		String json = """
				{
				    "firstName": "UpdatedUser",
				    "email": "user@gmail.com",
				    "role": "ADMIN"
				}
				""";

		mockMvc.perform(
				put("/user").header("X-User-Role", "ROLE_ADMIN").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.firstName").value("UpdatedUser"))
				.andExpect(jsonPath("$.lastName").value("User")).andExpect(jsonPath("$.email").value("user@gmail.com"))
				.andExpect(jsonPath("$.password").value("oldPassword")).andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	void updateUser_ShouldReturnError_WhenHeaderRoleIsNotAdmin() throws Exception {
		userRepository.save(new UserModel("User", "User", "user@gmail.com", "oldPassword", "USER"));

		String json = """
				{
				    "firstName": "UpdatedUser",
				    "email": "user@gmail.com"
				}
				""";

		mockMvc.perform(
				put("/user").header("X-User-Role", "ROLE_USER").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void deleteUser_ShouldDeleteUser_WhenUserExists() throws Exception {
		UserModel savedUser = userRepository.save(new UserModel("User", "User", "user@gmail.com", "user", "USER"));

		mockMvc.perform(delete("/user").param("id", savedUser.getId().toString())).andExpect(status().isOk())
				.andExpect(content().string("User with id " + savedUser.getId() + " is deleted"));
	}
}
