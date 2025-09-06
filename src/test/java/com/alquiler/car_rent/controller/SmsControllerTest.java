package com.alquiler.car_rent.controller;

import com.alquiler.car_rent.commons.dtos.LoginRequest;
import com.alquiler.car_rent.commons.dtos.TokenResponse;
import com.alquiler.car_rent.commons.dtos.UserEntityRequest;
import com.alquiler.car_rent.commons.enums.Role;
import com.alquiler.car_rent.service.impl.SmsServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SmsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SmsServiceImpl smsService;

    private String adminToken;

    private final String ADMIN_EMAIL = "admin@test.com";
    private final String ADMIN_PASSWORD = "password";

    @BeforeEach
    void setUp() throws Exception {
        adminToken = getToken(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    @Test
    void testSendSms_Success() throws Exception {
        String phoneNumber = "+1234567890";
        String message = "This is a test message";

        // Arrange: Configure the mock to do nothing when sendSms is called
        doNothing().when(smsService).sendSms(phoneNumber, message);

        // Act & Assert
        mockMvc.perform(post("/v1/sms/send")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("to", phoneNumber)
                        .param("message", message))
                .andExpect(status().isOk())
                .andExpect(content().string("Solicitud de envío de SMS procesada para " + phoneNumber));

        // Verify that the service method was called exactly once with the correct parameters
        verify(smsService, times(1)).sendSms(phoneNumber, message);
    }

    @Test
    void testSendSms_AsNonAdmin_Forbidden() throws Exception {
        String userEmail = "sms.user@example.com";
        String userPassword = "password123";
        createUserAndGetId(userEmail, "Sms User", Role.USERS, userPassword);
        String userToken = getToken(userEmail, userPassword);

        mockMvc.perform(post("/v1/sms/send")
                        .header("Authorization", "Bearer " + userToken)
                        .param("to", "+1234567890")
                        .param("message", "test"))
                .andExpect(status().isForbidden());

        // Verify the service was never called
        verify(smsService, never()).sendSms(anyString(), anyString());
    }

    @Test
    void testSendSms_NoToken_Forbidden() throws Exception {
        mockMvc.perform(post("/v1/sms/send")
                        .param("to", "+1234567890")
                        .param("message", "test"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSendSms_MissingPhoneNumber_BadRequest() throws Exception {
        mockMvc.perform(post("/v1/sms/send")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("message", "test"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSendSms_MissingMessage_BadRequest() throws Exception {
        mockMvc.perform(post("/v1/sms/send")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("to", "+1234567890"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSendSms_ServiceThrowsException_InternalServerError() throws Exception {
        String phoneNumber = "+1234567890";
        String message = "This will fail";

        // Arrange: Configure the mock to throw an exception
        doThrow(new RuntimeException("SMS Provider is down")).when(smsService).sendSms(phoneNumber, message);

        // Act & Assert
        mockMvc.perform(post("/v1/sms/send")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("to", phoneNumber)
                        .param("message", message))
                .andExpect(status().isInternalServerError());

        // Verify the service method was still called
        verify(smsService, times(1)).sendSms(phoneNumber, message);
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

    private Long createUserAndGetId(String email, String username, Role role, String password) throws Exception {
        // This helper needs an admin token to create a user, which is available from setUp()
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