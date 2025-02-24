package com.alquiler.car_rent.service;

import java.util.List;
import java.util.Optional;

import com.alquiler.car_rent.commons.dtos.RentalDto;

public interface RentalService {
	
	List<RentalDto>findAllRentals();
	Optional<RentalDto>findRentalById(Long id);
	RentalDto createRental (RentalDto rentalDto);
	RentalDto updateRental(Long id, RentalDto rentalDto);
    RentalDto cancelRental(Long id);
	void deleteRental(Long id);

}
