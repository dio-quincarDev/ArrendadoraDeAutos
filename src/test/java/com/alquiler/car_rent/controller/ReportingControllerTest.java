package com.alquiler.car_rent.controller;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.dtos.LoginRequest;
import com.alquiler.car_rent.commons.dtos.TokenResponse;
import com.alquiler.car_rent.commons.dtos.UserEntityRequest;
import com.alquiler.car_rent.commons.enums.Role;
import com.alquiler.car_rent.service.reportService.ReportingService;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("stg")
@Transactional
public class ReportingControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportingService reportingService;

    private String adminToken;

    private final String ADMIN_EMAIL = "testqa@gmail.com";
    private final String ADMIN_PASSWORD = "test-qa";

    @BeforeEach
    void setUp() throws Exception {
        adminToken = getToken(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    @Test
    void testGetTotalRentalsMetric_AsAdmin_Success() throws Exception {
        // Arrange: Mock the service layer
        // The controller calls a private method that then calls this service method
        byte[] mockJsonResponse = objectMapper.writeValueAsBytes(Map.of("totalRentals", 150L));
        when(reportingService.generateReport(
                any(ReportingConstants.OutputFormat.class),
                any(ReportingConstants.ReportType.class),
                any(), any(), any()))
                .thenReturn(mockJsonResponse);

        // Act & Assert
        mockMvc.perform(get("/v1/reports/metrics/total-rentals")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("period", "ANNUAL"))
                .andExpect(status().isOk())
                .andExpect(content().string("150"));
    }

    @Test
    void testGetTotalRentalsMetric_AsUser_Forbidden() throws Exception {
        // Arrange: Create a non-admin user and get their token
        String userEmail = "report.user@example.com";
        String userPassword = "password123";
        createUserAndGetId(userEmail, "Report User", Role.USERS, userPassword);
        String userToken = getToken(userEmail, userPassword);

        // Act & Assert
        mockMvc.perform(get("/v1/reports/metrics/total-rentals")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testExportReport_AsAdmin_Success() throws Exception {
        // Arrange
        byte[] dummyPdf = "dummy pdf content".getBytes();
        when(reportingService.generateReport(any(), any(), any(), any(), any()))
                .thenReturn(dummyPdf);

        // Act & Assert
        mockMvc.perform(get("/v1/reports/export")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("format", "PDF")
                        .param("reportType", "RENTAL_SUMMARY"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=")))
                .andExpect(content().bytes(dummyPdf));
    }

    @Test
    void testGetDashboardData_AsAdmin_Success() throws Exception {
        // Arrange
        Map<String, Object> dashboardData = Map.of(
                "totalRevenue", 50000.0,
                "totalRentals", 150L,
                "newCustomers", 25L
        );
        when(reportingService.generateReportData(any(), any(), any()))
                .thenReturn(dashboardData);

        // Act & Assert
        mockMvc.perform(get("/v1/reports")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalRevenue", org.hamcrest.Matchers.is(50000.0)))
                .andExpect(jsonPath("$.totalRentals", org.hamcrest.Matchers.is(150)))
                .andExpect(jsonPath("$.newCustomers", org.hamcrest.Matchers.is(25)));
    }

    // =================================================================================================================
    // HELPER Methods (Copied from SmsControllerTest)
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
