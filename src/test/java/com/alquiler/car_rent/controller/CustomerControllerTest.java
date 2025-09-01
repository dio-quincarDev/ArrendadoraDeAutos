package com.alquiler.car_rent.controller;


import com.alquiler.car_rent.commons.dtos.CustomerDto;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("stg")
@Transactional
public class CustomerControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void test_CreateCustomer_Success()throws Exception{
        String uniqueEmail = "jhonperez." + UUID.randomUUID() + "@gmail.com";
        CustomerDto newCustomer = new CustomerDto(
                null,
                "Jhon Perez",
                uniqueEmail,
                "PA22M23",
                null,
                null,
                "+50767757265",
                "ACTIVE"
        );

        String customerJson = objectMapper.writeValueAsString(newCustomer);

        mockMvc.perform(post("/v1/customers")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(customerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Jhon Perez"))
                .andExpect(jsonPath("$.email").value(uniqueEmail));
    }

    @Test
    void test_CreateCustomer_Fail_BadRequest() throws Exception {
        CustomerDto newCustomer = new CustomerDto(
                null,
                "", // Blank name
                "invalid-email", // Invalid email
                "PA22M23",
                null,
                null,
                "+50767757265",
                "ACTIVE"
        );

        String customerJson = objectMapper.writeValueAsString(newCustomer);

        mockMvc.perform(post("/v1/customers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_GetAllCustomers_Success() throws Exception {
        // Arrange: Create a customer first to ensure the list is not empty
        String uniqueEmail = "janedoe." + UUID.randomUUID() + "@gmail.com";
        CustomerDto newCustomer = new CustomerDto(null, "Jane Doe", uniqueEmail, "LKIU78", null, null, "+1234567890", "ACTIVE");
        mockMvc.perform(post("/v1/customers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isOk());

        // Act & Assert
        mockMvc.perform(get("/v1/customers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name=='Jane Doe')]", hasSize(1)));
    }

    @Test
    void test_GetAllCustomers_Fail_Forbidden() throws Exception {
        // Act & Assert: Perform request as an anonymous user, which should be forbidden
        mockMvc.perform(get("/v1/customers").with(anonymous()))
                .andExpect(status().isForbidden());
    }

    @Test
    void test_UpdateCustomer_Success() throws Exception {
        // Arrange: Create a customer first
        String originalEmail = "original." + UUID.randomUUID() + "@gmail.com";
        CustomerDto originalCustomer = new CustomerDto(null, "Original Name", originalEmail, "ORI123", null, null, "+50761111111", "ACTIVE");
        MvcResult result = mockMvc.perform(post("/v1/customers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originalCustomer)))
                .andExpect(status().isOk())
                .andReturn();
        Long customerId = objectMapper.readValue(result.getResponse().getContentAsString(), CustomerDto.class).id();

        // Act: Update the customer
        String updatedEmail = "updated." + UUID.randomUUID() + "@gmail.com";
        CustomerDto updatedCustomerDto = new CustomerDto(customerId, "Updated Name", updatedEmail, "UPD456", null, null, "+50762222222", "INACTIVE");
        mockMvc.perform(put("/v1/customers/" + customerId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCustomerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value(updatedEmail))
                .andExpect(jsonPath("$.customerStatus").value("INACTIVE"));
    }

    @Test
    void test_UpdateCustomer_Fail_NotFound() throws Exception {
        // Arrange: A customer DTO for a non-existent customer
        CustomerDto updatedCustomerDto = new CustomerDto(999L, "Ghost User", "ghost@gmail.com", "GHO789", null, null, "+50763333333", "ACTIVE");

        // Act & Assert
        mockMvc.perform(put("/v1/customers/999")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCustomerDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_DeleteCustomer_Success_And_Verify_Deletion() throws Exception {
        // Arrange: Create a new customer
        String uniqueEmail = "todelete." + UUID.randomUUID() + "@gmail.com";
        CustomerDto newCustomer = new CustomerDto(
                null,
                "User ToDelete",
                uniqueEmail,
                "DEL123",
                null, null, "+50769876543", "ACTIVE"
        );

        MvcResult result = mockMvc.perform(post("/v1/customers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Long customerId = objectMapper.readValue(responseBody, CustomerDto.class).id();

        // Act: Delete the customer with ADMIN role
        mockMvc.perform(delete("/v1/customers/" + customerId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());

        // Assert: Verify the customer is no longer found
        mockMvc.perform(get("/v1/customers/" + customerId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCustomerById_Success() throws Exception {
        // Arrange: Create a customer first
        String uniqueEmail = "findme." + UUID.randomUUID() + "@gmail.com";
        CustomerDto newCustomer = new CustomerDto(null, "Find Me", uniqueEmail, "FIN456", null, null, "+50769998888", "ACTIVE");
        MvcResult result = mockMvc.perform(post("/v1/customers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isOk())
                .andReturn();
        Long customerId = objectMapper.readValue(result.getResponse().getContentAsString(), CustomerDto.class).id();

        // Act & Assert
        mockMvc.perform(get("/v1/customers/" + customerId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId))
                .andExpect(jsonPath("$.name").value("Find Me"))
                .andExpect(jsonPath("$.email").value(uniqueEmail));
    }

    @Test
    void testGetCustomerById_Fail_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/customers/99999")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateCustomer_Fail_DuplicateEmail() throws Exception {
        // Arrange: Create a customer
        String duplicateEmail = "duplicate." + UUID.randomUUID() + "@gmail.com";
        CustomerDto customer1 = new CustomerDto(null, "First User", duplicateEmail, "DUP111", null, null, "+50761111111", "ACTIVE");
        mockMvc.perform(post("/v1/customers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer1)))
                .andExpect(status().isOk());

        // Act & Assert: Try to create another customer with the same email
        CustomerDto customer2 = new CustomerDto(null, "Second User", duplicateEmail, "DUP222", null, null, "+50762222222", "ACTIVE");
        mockMvc.perform(post("/v1/customers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer2)))
                .andExpect(status().isBadRequest());
    }
}
