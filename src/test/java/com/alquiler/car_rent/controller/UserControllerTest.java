package com.alquiler.car_rent.controller;

import com.alquiler.car_rent.commons.dtos.LoginRequest;
import com.alquiler.car_rent.commons.dtos.TokenResponse;
import com.alquiler.car_rent.commons.dtos.UserEntityRequest;
import com.alquiler.car_rent.commons.enums.Role;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("stg")
@Transactional
public class UserControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;

    // Credentials from AbstractIntegrationTest
    private final String ADMIN_EMAIL = "testqa@gmail.com";
    private final String ADMIN_PASSWORD = "test-qa";

    @BeforeEach
    void setUp() throws Exception {
        adminToken = getToken(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    // =================================================================================================================
    // CREATE User Tests
    // =================================================================================================================

    @Test
    void testCreateUser_Success() throws Exception {
        String uniqueEmail = "new.user." + UUID.randomUUID() + "@example.com";
        UserEntityRequest newUserRequest = UserEntityRequest.builder()
                .username("New Test User")
                .email(uniqueEmail)
                .password("strongPassword123")
                .role(Role.USERS)
                .build();

        mockMvc.perform(post("/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email", is(uniqueEmail)))
                .andExpect(jsonPath("$.role", is("USERS")));
    }

    @Test
    void testCreateUser_EmailAlreadyExists_BadRequest() throws Exception {
        UserEntityRequest newUserRequest = UserEntityRequest.builder()
                .username("Existing User")
                .email(ADMIN_EMAIL) // Use an email that already exists
                .password("password123")
                .role(Role.USERS)
                .build();

        mockMvc.perform(post("/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isBadRequest());
    }

    // =================================================================================================================
    // GET User Tests
    // =================================================================================================================

    @Test
    void testGetAllUsers_Success() throws Exception {
        mockMvc.perform(get("/v1/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))); // Expecting only the initial admin user
    }

    @Test
    void testGetUserById_Success() throws Exception {
        Long userId = createUserAndGetId("user.for.get@example.com", "Get User", Role.USERS);

        mockMvc.perform(get("/v1/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.intValue())))
                .andExpect(jsonPath("$.email", is("user.for.get@example.com")));
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        mockMvc.perform(get("/v1/users/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // =================================================================================================================
    // UPDATE User Tests
    // =================================================================================================================

    @Test
    void testUpdateUser_Success() throws Exception {
        Long userId = createUserAndGetId("user.to.update@example.com", "Original Name", Role.USERS);
        UserEntityRequest updateRequest = UserEntityRequest.builder().username("Updated Name").build();

        mockMvc.perform(put("/v1/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.intValue())))
                .andExpect(jsonPath("$.username", is("Updated Name")));
    }

    @Test
    void testUpdateUser_NotFound() throws Exception {
        UserEntityRequest updateRequest = UserEntityRequest.builder().username("Updated Name").build();

        mockMvc.perform(put("/v1/users/99999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    // =================================================================================================================
    // DELETE User Tests
    // =================================================================================================================

    @Test
    void testDeleteUser_Success() throws Exception {
        Long userId = createUserAndGetId("user.to.delete@example.com", "Delete Me", Role.USERS);

        // Delete the user
        mockMvc.perform(delete("/v1/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify the user is gone
        mockMvc.perform(get("/v1/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteUser_NotFound() throws Exception {
        mockMvc.perform(delete("/v1/users/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // =================================================================================================================
    // UPDATE ROLE User Tests
    // =================================================================================================================

    @Test
    void testUpdateUserRole_Success() throws Exception {
        Long userId = createUserAndGetId("user.role.update@example.com", "Role Changer", Role.USERS);

        mockMvc.perform(put("/v1/users/" + userId + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("newRole", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    void testUpdateUserRole_InvalidRole_BadRequest() throws Exception {
        Long userId = createUserAndGetId("user.invalid.role@example.com", "Invalid Role User", Role.USERS);

        mockMvc.perform(put("/v1/users/" + userId + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("newRole", "INVALID_ROLE"))
                .andExpect(status().isBadRequest());
    }

    // =================================================================================================================
    // SECURITY Tests
    // =================================================================================================================

    @Test
    void testAccessEndpoint_AsNonAdminUser_Forbidden() throws Exception {
        // Create a standard user and get their token
        String userEmail = "standard.user@example.com";
        String userPassword = "password123";
        createUserAndGetId(userEmail, "Standard User", Role.USERS, userPassword);
        userToken = getToken(userEmail, userPassword);

        // Attempt to access an admin-only endpoint with the user's token
        mockMvc.perform(get("/v1/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }


    // =================================================================================================================
    // HELPER Methods
    // =================================================================================================================

    private String getToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        TokenResponse tokenResponse = objectMapper.readValue(responseBody, TokenResponse.class);
        return tokenResponse.getAccesToken();
    }

    private Long createUserAndGetId(String email, String username, Role role) throws Exception {
        return createUserAndGetId(email, username, role, "defaultPassword123");
    }

    private Long createUserAndGetId(String email, String username, Role role, String password) throws Exception {
        UserEntityRequest newUserRequest = UserEntityRequest.builder()
                .username(username)
                .email(email)
                .password(password)
                .role(role)
                .build();

        MvcResult result = mockMvc.perform(post("/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("id").asLong();
    }
}
