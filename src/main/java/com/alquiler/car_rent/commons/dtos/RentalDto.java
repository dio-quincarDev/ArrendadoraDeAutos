package com.alquiler.car_rent.commons.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.alquiler.car_rent.commons.enums.RentalStatus;




public record RentalDto(
		Long id,
		Long customerId,
		String customerName,
		Long vehicleId,
		String vehicleModel,
		String vehicleBrand,
		RentalStatus rentalStatus,
		LocalDateTime startDate,
		LocalDateTime endDate,
		BigDecimal totalPrice,
		LocalDateTime createdAt
		) {
	}
	