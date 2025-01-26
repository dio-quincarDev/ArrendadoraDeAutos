package service;

import java.util.List;
import java.util.Optional;

import commons.entities.Rental;

public interface RentalService {
	
	List<Rental>findAllRentals();
	Optional<Rental>findRentalById(Long id);
	Rental createRental (Rental rental);
	Rental updateRental(Long id, Rental rental);
	void deleteRental(Long id);

}
