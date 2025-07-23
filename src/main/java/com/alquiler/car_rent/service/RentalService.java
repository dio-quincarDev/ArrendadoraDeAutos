package com.alquiler.car_rent.service;

import com.alquiler.car_rent.commons.dtos.RentalDto;

import java.util.List;

public interface RentalService {
	
	List<RentalDto>findAllRentals();
	RentalDto findRentalById(Long id);
	RentalDto createRental (RentalDto rentalDto);
	RentalDto updateRental(Long id, RentalDto rentalDto);
    RentalDto cancelRental(Long id);
	void deleteRental(Long id);

}
