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
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
