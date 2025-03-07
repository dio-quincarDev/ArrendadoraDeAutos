package com.alquiler.car_rent.commons.dtos;

import java.time.LocalDateTime;

public record CustomerDto(
        Long id, 
        String name, 
        String email, 
        String license,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String phone,
        String customerStatus) {
}
