package com.alquiler.car_rent.controller;

import com.alquiler.car_rent.commons.dtos.LoginRequest;
import com.alquiler.car_rent.commons.dtos.TokenResponse;
import com.alquiler.car_rent.commons.dtos.UserEntityRequest;
import com.alquiler.car_rent.commons.enums.Role;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    // Credentials from AbstractIntegrationTest
    private final String ADMIN_EMAIL = "testqa@gmail.com";
    private final String ADMIN_PASSWORD = "test-qa";

    @BeforeEach
    void setUp() throws Exception {
        // Log in as admin to get a token valid for all tests in this class
        adminToken = getAdminToken();
    }

    @Test
    void testCreateUser_Success() throws Exception {
        // Arrange
        String uniqueEmail = "new.user." + UUID.randomUUID() + "@example.com";
        UserEntityRequest newUserRequest = UserEntityRequest.builder()
                .username("New Test User")
                .email(uniqueEmail)
                .password("strongPassword123")
                .role(Role.USERS)
                .build();

        // Act & Assert
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
    void testGetAllUsers_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))); // Expecting only the initial admin user
    }

    private String getAdminToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD);
        MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        TokenResponse tokenResponse = objectMapper.readValue(responseBody, TokenResponse.class);
        return tokenResponse.getAccesToken();
    }
}