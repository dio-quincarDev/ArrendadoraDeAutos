package com.alquiler.car_rent.commons.dtos;

import java.time.LocalDateTime;

import com.alquiler.car_rent.commons.enums.VehicleStatus;

public record VehicleDto(
		Long id,
		String brand,
		String model,
		Integer year,
		String plate,
		VehicleStatus status,
		LocalDateTime createdAt) {
	
		
	}
