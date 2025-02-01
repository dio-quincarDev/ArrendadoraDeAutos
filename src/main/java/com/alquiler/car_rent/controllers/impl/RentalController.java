package com.alquiler.car_rent.controllers.impl;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.alquiler.car_rent.commons.dtos.RentalDto;
import com.alquiler.car_rent.commons.mappers.RentalMapper;
import com.alquiler.car_rent.controllers.RentalApi;
import com.alquiler.car_rent.service.RentalService;

@RestController
public class RentalController implements RentalApi {
	
	private final RentalService rentalService;
	private final RentalMapper rentalMapper;
	
	public RentalController(RentalService rentalService, RentalMapper rentalMapper) {
        this.rentalService = rentalService;
        this.rentalMapper = rentalMapper;
    }

	@Override
	public ResponseEntity<RentalDto> createRental(RentalDto rentalDto) {
		RentalDto createdRental = rentalService.createRental(rentalDto);
		return ResponseEntity.ok(createdRental);
	}

	@Override
	public ResponseEntity<List<RentalDto>> getAllRentals() {
		List<RentalDto> rentals = rentalService.findAllRentals();
		return ResponseEntity.ok(rentals);
	}

	@Override
	public ResponseEntity<RentalDto> getRentalById(Long id, RentalDto rentalDto) {
		return rentalService.findRentalById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<RentalDto> updateRental(Long id, RentalDto rentalDto) {
		RentalDto updatedRental = rentalService.updateRental(id, rentalDto);
		return ResponseEntity.ok(updatedRental);
	}
	
	@Override
	 public ResponseEntity<RentalDto> cancelRental(Long id) {
	    RentalDto cancelledRental = rentalService.cancelRental(id);
	    return ResponseEntity.ok(cancelledRental);
    }

	@Override
	public ResponseEntity<Void> deleteRental(Long id) {
		rentalService.deleteRental(id);
		return ResponseEntity.noContent().build();
	}

}
