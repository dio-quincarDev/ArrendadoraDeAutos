package com.alquiler.car_rent.commons.dtos;

import java.time.LocalDateTime;

import com.alquiler.car_rent.commons.entities.Customer;

public record CustomerDto(
        Long id, 
        String name, 
        String email, 
        String license, 
        String phone,
        String customerStatus, 
        LocalDateTime createdAt, 
        LocalDateTime updatedAt) {

    public static CustomerDto fromEntity(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getLicense(),
                customer.getPhone(),
                customer.getCustomerStatus().toString(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
