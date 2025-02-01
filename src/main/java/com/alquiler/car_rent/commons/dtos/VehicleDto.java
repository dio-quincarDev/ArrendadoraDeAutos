package com.alquiler.car_rent.commons.dtos;

import java.time.LocalDateTime;

import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.commons.enums.VehicleStatus;

public record VehicleDto(
		Long id,
		String brand,
		String model,
		int year,
		String plate,
		VehicleStatus status,
		LocalDateTime createdAt) {
	
		
	}
