package com.alquiler.car_rent.service;

import java.util.List;
import java.util.Optional;

import com.alquiler.car_rent.commons.entities.Rental;

public interface RentalService {
	
	List<Rental>findAllRentals();
	Optional<Rental>findRentalById(Long id);
	Rental createRental (Rental rental);
	Rental updateRental(Long id, Rental rental);
    Rental cancelRental(Long id);
	void deleteRental(Long id);

}
