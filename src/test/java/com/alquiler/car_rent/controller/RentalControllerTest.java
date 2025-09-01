package com.alquiler.car_rent.controller;

import com.alquiler.car_rent.commons.dtos.CustomerDto;
import com.alquiler.car_rent.commons.dtos.RentalDto;
import com.alquiler.car_rent.commons.dtos.VehicleDto;
import com.alquiler.car_rent.commons.enums.PricingTier;
import com.alquiler.car_rent.commons.enums.VehicleStatus;
import com.alquiler.car_rent.commons.enums.VehicleType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("stg")
@Transactional
public class RentalControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerDto testCustomer;
    private VehicleDto testVehicle;

    @BeforeEach
    void setUp() throws Exception {
        // Create a customer and a vehicle to be used in rental tests
        testCustomer = createTestCustomer();
        testVehicle = createTestVehicle();
    }

    @Test
    void testCreateRental_Success() throws Exception {
        // Arrange
        RentalDto newRental = new RentalDto();
        newRental.setId(0L); // Dummy data to pass validation
        newRental.setCustomerName(testCustomer.name()); // Dummy data to pass validation
        newRental.setCustomerId(testCustomer.id());
        newRental.setVehicleId(testVehicle.getId());
        newRental.setStartDate(LocalDateTime.now().plusDays(1));
        newRental.setEndDate(LocalDateTime.now().plusDays(3));
        newRental.setChosenPricingTier(PricingTier.STANDARD);

        // Act & Assert
        mockMvc.perform(post("/v1/rentals")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRental)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerId").value(testCustomer.id()))
                .andExpect(jsonPath("$.vehicleId").value(testVehicle.getId()))
                .andExpect(jsonPath("$.rentalStatus").value("ACTIVE"));
    }

    @Test
    void testCreateRental_Fail_BadRequest() throws Exception {
        // Arrange: Invalid rental DTO (missing customerId and vehicleId)
        RentalDto newRental = new RentalDto();
        newRental.setId(0L); // Dummy data to pass validation for other fields
        newRental.setCustomerName("Test"); // Dummy data to pass validation for other fields
        newRental.setStartDate(LocalDateTime.now().plusDays(1));
        newRental.setEndDate(LocalDateTime.now().plusDays(3));
        newRental.setChosenPricingTier(PricingTier.STANDARD);

        // Act & Assert
        mockMvc.perform(post("/v1/rentals")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRental)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testCreateRental_Fail_Forbidden() throws Exception {
        // Arrange
        RentalDto newRental = new RentalDto();
        newRental.setId(0L);
        newRental.setCustomerName(testCustomer.name());
        newRental.setCustomerId(testCustomer.id());
        newRental.setVehicleId(testVehicle.getId());
        newRental.setStartDate(LocalDateTime.now().plusDays(1));
        newRental.setEndDate(LocalDateTime.now().plusDays(3));
        newRental.setChosenPricingTier(PricingTier.STANDARD);

        // Act & Assert
        mockMvc.perform(post("/v1/rentals")
                        .with(anonymous()) // Perform as anonymous user
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRental)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetRentalById_Success() throws Exception {
        // Arrange: Create a rental first
        Long rentalId = createTestRentalAndGetId();

        // Act & Assert
        mockMvc.perform(get("/v1/rentals/" + rentalId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rentalId))
                .andExpect(jsonPath("$.customerId").value(testCustomer.id()));
    }

    @Test
    void testGetRentalById_Fail_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/rentals/9999")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCancelRental_Success() throws Exception {
        // Arrange: Create a rental first
        Long rentalId = createTestRentalAndGetId();

        // Act
        mockMvc.perform(put("/v1/rentals/" + rentalId + "/cancel")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentalStatus").value("CANCELLED"));
    }

    @Test
    void testUpdateRental_Success() throws Exception {
        // Arrange: Create a rental first
        Long rentalId = createTestRentalAndGetId();

        // Prepare updated data - must include all @NotNull/@NotBlank fields to pass validation
        RentalDto updatedRentalDto = new RentalDto();
        updatedRentalDto.setId(rentalId); // Mandatory field
        updatedRentalDto.setCustomerId(testCustomer.id()); // Mandatory field
        updatedRentalDto.setCustomerName(testCustomer.name()); // Mandatory field
        updatedRentalDto.setVehicleId(testVehicle.getId()); // Mandatory field

        // Truncate to seconds to avoid nanosecond precision issues in JSON comparison
        LocalDateTime startDate = LocalDateTime.now().plusDays(2).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);

        updatedRentalDto.setStartDate(startDate); // Mandatory and updated field
        updatedRentalDto.setEndDate(endDate);   // Mandatory and updated field
        updatedRentalDto.setChosenPricingTier(PricingTier.PREMIUM); // Updated field

        // Define the expected date format, matching the @JsonFormat in the DTO
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Act & Assert
        mockMvc.perform(put("/v1/rentals/" + rentalId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRentalDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rentalId))
                .andExpect(jsonPath("$.chosenPricingTier").value(PricingTier.PREMIUM.toString()))
                .andExpect(jsonPath("$.endDate").value(endDate.format(formatter)));
    }

    @Test
    void testUpdateRental_Fail_BadRequest() throws Exception {
        // Arrange: Create a rental first
        Long rentalId = createTestRentalAndGetId();

        // Prepare invalid data (start date after end date)
        RentalDto invalidRentalDto = new RentalDto();
        invalidRentalDto.setStartDate(LocalDateTime.now().plusDays(5));
        invalidRentalDto.setEndDate(LocalDateTime.now().plusDays(2));
        invalidRentalDto.setChosenPricingTier(PricingTier.STANDARD);

        // Act & Assert
        mockMvc.perform(put("/v1/rentals/" + rentalId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRentalDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteRental_Fail_ForbiddenForUserRole() throws Exception {
        // Arrange: Create a rental first
        Long rentalId = createTestRentalAndGetId();

        // Act & Assert
        mockMvc.perform(delete("/v1/rentals/" + rentalId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteRental_Success_ForAdminRole() throws Exception {
        // Arrange: Create a rental first
        Long rentalId = createTestRentalAndGetId();

        // Act & Assert
        mockMvc.perform(delete("/v1/rentals/" + rentalId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/v1/rentals/" + rentalId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS"))))
                .andExpect(status().isNotFound());
    }

    // ============== Helper Methods to create prerequisite data ==============

    private CustomerDto createTestCustomer() throws Exception {
        String uniqueEmail = "test.customer." + UUID.randomUUID() + "@example.com";
        CustomerDto newCustomer = new CustomerDto(null, "Test Customer", uniqueEmail, "ID12345", null, null, "+123456789", "ACTIVE");
        
        MvcResult result = mockMvc.perform(post("/v1/customers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isOk())
                .andReturn();
        
        return objectMapper.readValue(result.getResponse().getContentAsString(), CustomerDto.class);
    }

    private VehicleDto createTestVehicle() throws Exception {
        VehicleDto newVehicle = new VehicleDto();
        newVehicle.setBrand("TestBrand");
        newVehicle.setModel("TestModel" + UUID.randomUUID());
        newVehicle.setPlate("TT-1234");
        newVehicle.setActualDailyRate(new BigDecimal("100.00"));
        newVehicle.setStatus(VehicleStatus.valueOf("AVAILABLE"));
        newVehicle.setVehicleType(VehicleType.valueOf("SEDAN"));
        newVehicle.setYear(2023);
        newVehicle.setPricingTier(PricingTier.STANDARD);

        MvcResult result = mockMvc.perform(post("/v1/vehicles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newVehicle)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), VehicleDto.class);
    }

    private Long createTestRentalAndGetId() throws Exception {
        RentalDto newRental = new RentalDto();
        newRental.setId(0L); // Dummy data to pass validation
        newRental.setCustomerName(testCustomer.name()); // Dummy data to pass validation
        newRental.setCustomerId(testCustomer.id());
        newRental.setVehicleId(testVehicle.getId());
        newRental.setStartDate(LocalDateTime.now().plusDays(1));
        newRental.setEndDate(LocalDateTime.now().plusDays(3));
        newRental.setChosenPricingTier(PricingTier.STANDARD);

        MvcResult result = mockMvc.perform(post("/v1/rentals")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRental)))
                .andExpect(status().isOk())
                .andReturn();

        RentalDto createdRental = objectMapper.readValue(result.getResponse().getContentAsString(), RentalDto.class);
        return createdRental.getId();
    }
}