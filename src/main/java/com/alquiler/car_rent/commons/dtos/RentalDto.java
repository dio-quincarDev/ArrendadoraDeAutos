package com.alquiler.car_rent.commons.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.alquiler.car_rent.commons.entities.Rental;




public record RentalDto(
		Long id,
		Long customerId,
		Long vehicleId,
		String rentalStatus,
		LocalDateTime startDate,
		LocalDateTime endDate,
		BigDecimal totalPrice,
		LocalDateTime createdAt
		) {
	public static RentalDto fromEntity(Rental rental) {
		return new RentalDto(
				rental.getId(),
				rental.getCustomer().getId(),
				rental.getVehicle().getId(),
				rental.getRentalStatus().toString(),
				rental.getStartDate(),
				rental.getEndDate(),
				rental.getTotalPrice(),
				rental.getCreatedAt()
				);
	}
	

}
