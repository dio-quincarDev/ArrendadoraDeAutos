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
        CustomerDto newCustomer = new CustomerDto(
                null,
                "Jhon Perez",
                "jhonperz@gmail.com",
                "PA22M23",
                null,
                null,
                "+5077757265",
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
                .andExpect(jsonPath("$.email").value("jhonperz@gmail.com"));
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
                "+5077757265",
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
        CustomerDto newCustomer = new CustomerDto(null, "Jane Doe", "janedoe@gmail.com", "LKIU78", null, null, "+123456789", "ACTIVE");
        mockMvc.perform(post("/v1/customers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isOk());

        // Act & Assert
        mockMvc.perform(get("/v1/customers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Jane Doe"));
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
        CustomerDto originalCustomer = new CustomerDto(null, "Original Name", "original@gmail.com", "ORI123", null, null, "+5071111111", "ACTIVE");
        MvcResult result = mockMvc.perform(post("/v1/customers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originalCustomer)))
                .andExpect(status().isOk())
                .andReturn();
        Long customerId = objectMapper.readValue(result.getResponse().getContentAsString(), CustomerDto.class).id();

        // Act: Update the customer
        CustomerDto updatedCustomerDto = new CustomerDto(customerId, "Updated Name", "updated@gmail.com", "UPD456", null, null, "+5072222222", "INACTIVE");
        mockMvc.perform(put("/v1/customers/" + customerId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USERS")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCustomerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@gmail.com"))
                .andExpect(jsonPath("$.customerStatus").value("INACTIVE"));
    }

    @Test
    void test_UpdateCustomer_Fail_NotFound() throws Exception {
        // Arrange: A customer DTO for a non-existent customer
        CustomerDto updatedCustomerDto = new CustomerDto(999L, "Ghost User", "ghost@gmail.com", "GHO789", null, null, "+333333", "ACTIVE");

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
        CustomerDto newCustomer = new CustomerDto(
                null,
                "User ToDelete",
                "todelete@gmail.com",
                "DEL123",
                null, null, "+987654321", "ACTIVE"
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
}
