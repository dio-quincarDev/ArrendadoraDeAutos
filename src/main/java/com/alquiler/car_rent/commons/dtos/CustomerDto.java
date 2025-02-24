package com.alquiler.car_rent.commons.dtos;


public record CustomerDto(
        Long id, 
        String name, 
        String email, 
        String license, 
        String phone,
        String customerStatus) {
}
