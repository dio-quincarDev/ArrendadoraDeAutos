package com.alquiler.car_rent.controllers.impl;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.controllers.RentalApi;
import com.alquiler.car_rent.service.RentalService;

@RestController
public class RentalController implements RentalApi {
	
	private final RentalService rentalService;
	
	public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

	@Override
	public ResponseEntity<Rental> createRental(Rental rental) {
		Rental createdRental = rentalService.createRental(rental);
		return ResponseEntity.ok(createdRental);
	}

	@Override
	public ResponseEntity<List<Rental>> getAllRentals() {
		List<Rental> rentals = rentalService.findAllRentals();
		return ResponseEntity.ok(rentals);
	}

	@Override
	public ResponseEntity<Rental> getRentalById(Long id, Rental rental) {
		return rentalService.findRentalById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<Rental> updateRental(Long id, Rental rental) {
		Rental updatedRental = rentalService.updateRental(id, rental);
		return ResponseEntity.ok(updatedRental);
	}
	
	@Override
	 public ResponseEntity<Rental> cancelRental(Long id) {
	    Rental cancelledRental = rentalService.cancelRental(id);
	    return ResponseEntity.ok(cancelledRental);
    }

	@Override
	public ResponseEntity<Void> deleteRental(Long id) {
		rentalService.deleteRental(id);
		return ResponseEntity.noContent().build();
	}

}
