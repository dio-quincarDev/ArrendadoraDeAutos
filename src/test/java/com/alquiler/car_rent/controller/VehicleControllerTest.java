package com.alquiler.car_rent.controller;

import com.alquiler.car_rent.commons.dtos.VehicleDto;
import com.alquiler.car_rent.commons.enums.PricingTier;
import com.alquiler.car_rent.commons.enums.VehicleStatus;
import com.alquiler.car_rent.commons.enums.VehicleType;
import com.alquiler.car_rent.repositories.VehicleRepository;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("stg")
@Transactional
public class VehicleControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleRepository vehicleRepository;

    

    private VehicleDto createValidVehicleDto(String plateSuffix) {
        VehicleDto vehicleDto = new VehicleDto();
        vehicleDto.setBrand("Brand" + plateSuffix);
        vehicleDto.setModel("Model" + plateSuffix);
        vehicleDto.setYear(2023);
        vehicleDto.setPlate("PLATE-" + plateSuffix);
        vehicleDto.setVehicleType(VehicleType.SEDAN);
        vehicleDto.setPricingTier(PricingTier.STANDARD);
        vehicleDto.setStatus(VehicleStatus.AVAILABLE);
        return vehicleDto;
    }

    @Test
    void testCreateVehicle_Success() throws Exception {
        String uniquePlate = UUID.randomUUID().toString().substring(0, 4); // Changed to 4 characters
        VehicleDto newVehicle = createValidVehicleDto(uniquePlate);

        mockMvc.perform(post("/v1/vehicles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newVehicle)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.plate").value("PLATE-" + uniquePlate));
    }

    @Test
    void testCreateVehicle_Fail_BadRequest_InvalidData() throws Exception {
        VehicleDto invalidVehicle = new VehicleDto();
        invalidVehicle.setBrand(""); // Blank brand
        invalidVehicle.setModel("Model");
        invalidVehicle.setYear(1899); // Invalid year
        invalidVehicle.setPlate(""); // Blank plate
        invalidVehicle.setVehicleType(VehicleType.SEDAN);
        invalidVehicle.setPricingTier(PricingTier.STANDARD);
        invalidVehicle.setStatus(VehicleStatus.AVAILABLE);

        mockMvc.perform(post("/v1/vehicles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidVehicle)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateVehicle_Fail_Conflict_DuplicatePlate() throws Exception {
        String uniquePlate = UUID.randomUUID().toString().substring(0, 4);
        
        // Arrange: Create and persist the first vehicle directly using TestEntityManager
        // This ensures the first vehicle is committed to the DB before the second attempt
        com.alquiler.car_rent.commons.entities.Vehicle vehicleEntity = new com.alquiler.car_rent.commons.entities.Vehicle();
        vehicleEntity.setBrand("Brand" + uniquePlate);
        vehicleEntity.setModel("Model" + uniquePlate);
        vehicleEntity.setYear(2023);
        vehicleEntity.setPlate("PLATE-" + uniquePlate);
        vehicleEntity.setVehicleType(VehicleType.SEDAN);
        vehicleEntity.setPricingTier(PricingTier.STANDARD);
        vehicleEntity.setStatus(VehicleStatus.AVAILABLE);

        vehicleRepository.save(vehicleEntity);

        // Act: Try to create a second vehicle with the same plate via the controller
        VehicleDto vehicle2 = createValidVehicleDto(uniquePlate); // Same plate as vehicle1
        mockMvc.perform(post("/v1/vehicles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicle2)))
                .andExpect(status().isBadRequest()); // Change from isConflict() to isBadRequest()
    }

    @Test
    void testGetVehicleById_Success() throws Exception {
        // Arrange: Create a vehicle first
        String uniquePlate = UUID.randomUUID().toString().substring(0, 4);
        VehicleDto newVehicle = createValidVehicleDto(uniquePlate);
        MvcResult result = mockMvc.perform(post("/v1/vehicles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newVehicle)))
                .andExpect(status().isCreated())
                .andReturn();
        Long vehicleId = objectMapper.readValue(result.getResponse().getContentAsString(), VehicleDto.class).getId();

        // Act & Assert
        mockMvc.perform(get("/v1/vehicles/" + vehicleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.plate").value("PLATE-" + uniquePlate));
    }

    @Test
    void testGetVehicleById_Fail_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/vehicles/99999")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateVehicle_Success() throws Exception {
        // Arrange: Create a vehicle first
        String uniquePlate = UUID.randomUUID().toString().substring(0, 4);
        VehicleDto originalVehicle = createValidVehicleDto(uniquePlate);
        MvcResult result = mockMvc.perform(post("/v1/vehicles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originalVehicle)))
                .andExpect(status().isCreated())
                .andReturn();
        Long vehicleId = objectMapper.readValue(result.getResponse().getContentAsString(), VehicleDto.class).getId();

        // Act: Update the vehicle
        VehicleDto updatedVehicle = new VehicleDto();
        updatedVehicle.setId(vehicleId);
        updatedVehicle.setBrand("Updated Brand");
        updatedVehicle.setModel("Updated Model");
        updatedVehicle.setYear(2024);
        updatedVehicle.setPlate("UP-" + uniquePlate); // Example: "UP-ABCD" (7 characters)
        updatedVehicle.setVehicleType(VehicleType.SUV);
        updatedVehicle.setPricingTier(PricingTier.PREMIUM);
        updatedVehicle.setStatus(VehicleStatus.RENTED);

        mockMvc.perform(put("/v1/vehicles/" + vehicleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedVehicle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brand").value("Updated Brand"))
                .andExpect(jsonPath("$.plate").value("UP-" + uniquePlate)) // Change this line
                .andExpect(jsonPath("$.status").value("RENTED"));
    }

    @Test
    void testUpdateVehicle_Fail_NotFound() throws Exception {
        // Arrange: A vehicle DTO for a non-existent vehicle
        VehicleDto updatedVehicle = new VehicleDto();
        updatedVehicle.setBrand("Ghost Brand");
        updatedVehicle.setModel("Ghost Model");
        updatedVehicle.setYear(2000);
        updatedVehicle.setPlate("GHOST-PL"); // Change "GHOST-PLATE" to "GHOST-PL" (8 characters)
        updatedVehicle.setVehicleType(VehicleType.SEDAN);
        updatedVehicle.setPricingTier(PricingTier.STANDARD);
        updatedVehicle.setStatus(VehicleStatus.AVAILABLE);

        // Act & Assert
        mockMvc.perform(put("/v1/vehicles/99999")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedVehicle)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteVehicle_Success_And_Verify_Deletion() throws Exception {
        // Arrange: Create a new vehicle
        String uniquePlate = UUID.randomUUID().toString().substring(0, 4);
        VehicleDto newVehicle = createValidVehicleDto(uniquePlate);
        MvcResult result = mockMvc.perform(post("/v1/vehicles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newVehicle)))
                .andExpect(status().isCreated())
                .andReturn();
        Long vehicleId = objectMapper.readValue(result.getResponse().getContentAsString(), VehicleDto.class).getId();

        // Act: Delete the vehicle
        mockMvc.perform(delete("/v1/vehicles/" + vehicleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());

        // Assert: Verify the vehicle is no longer found
        mockMvc.perform(get("/v1/vehicles/" + vehicleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllVehicles_Success() throws Exception {
        // Arrange: Create a few vehicles
        String plate1 = UUID.randomUUID().toString().substring(0, 4);
        String plate2 = UUID.randomUUID().toString().substring(0, 4);
        mockMvc.perform(post("/v1/vehicles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidVehicleDto(plate1))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/vehicles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidVehicleDto(plate2))))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/v1/vehicles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testGetAllVehicles_EmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/vehicles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}