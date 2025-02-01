package com.alquiler.car_rent.commons.dtos;

import java.time.LocalDateTime;

import com.alquiler.car_rent.commons.entities.Vehicle;

public record VehicleDto(
		Long id,
		String brand,
		String model,
		int year,
		String plate,
		String status,
		LocalDateTime createdAt) {
	public static VehicleDto fromEntity (Vehicle vehicle){
		return new VehicleDto(
				vehicle.getId(),
				vehicle.getBrand(),
				vehicle.getModel(),
				vehicle.getYear(),
				vehicle.getPlate(),
				vehicle.getStatus().toString(),
				vehicle.getCreatedAt()
				);
		
	}


}
