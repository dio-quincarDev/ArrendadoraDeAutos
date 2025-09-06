package com.alquiler.car_rent.controller;

import com.alquiler.car_rent.commons.dtos.LoginRequest;
import com.alquiler.car_rent.commons.dtos.TokenResponse;
import com.alquiler.car_rent.commons.dtos.UserEntityRequest;
import com.alquiler.car_rent.commons.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Credentials from AbstractIntegrationTest
    private final String ADMIN_EMAIL = "admin@test.com";
    private final String ADMIN_PASSWORD = "password";

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD);

        // Act & Assert
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accesToken", notNullValue()));
    }

    @Test
    void testLogin_Fail_InvalidCredentials() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(ADMIN_EMAIL, "wrong-password");

        // Act & Assert
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_Fail_InvalidEmailFormat() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("not-an-email", ADMIN_PASSWORD);

        // Act & Assert
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_Fail_BlankPassword() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(ADMIN_EMAIL, "");

        // Act & Assert
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_Success() throws Exception {
        // Arrange
        String uniqueEmail = "test.user." + UUID.randomUUID() + "@example.com";
        UserEntityRequest newUser = UserEntityRequest.builder()
                .username("Test User")
                .email(uniqueEmail)
                .password("password123")
                .role(Role.USERS)
                .build();

        // Act & Assert
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accesToken", notNullValue()));
    }

    @Test
    void testAccessProtectedEndpoint_Success() throws Exception {
        // Arrange: First, log in to get a valid token
        LoginRequest loginRequest = new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD);
        MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        TokenResponse tokenResponse = objectMapper.readValue(responseBody, TokenResponse.class);
        String token = tokenResponse.getAccesToken();

        // Act & Assert: Access the protected endpoint with the token
        mockMvc.perform(get("/v1/auth")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue())); // Expecting the user ID string
    }

    @Test
    void testRegister_Fail_EmailAlreadyExists() throws Exception {
        // Arrange: Use the admin email which is guaranteed to exist
        UserEntityRequest newUser = UserEntityRequest.builder()
                .username("Another User")
                .email(ADMIN_EMAIL) // Existing email
                .password("password123")
                .role(Role.USERS)
                .build();

        // Act & Assert
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAccessProtectedEndpoint_Fail_NoToken() throws Exception {
        // Act & Assert: Access the protected endpoint without any Authorization header
        mockMvc.perform(get("/v1/auth"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAccessProtectedEndpoint_Fail_InvalidToken() throws Exception {
        // Arrange
        String invalidToken = "this-is-not-a-valid-jwt";

        // Act & Assert: Access the protected endpoint with a bad token
        mockMvc.perform(get("/v1/auth")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isForbidden());
    }
}

