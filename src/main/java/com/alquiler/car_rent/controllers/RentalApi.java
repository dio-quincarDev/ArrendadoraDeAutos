package com.alquiler.car_rent.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;
import com.alquiler.car_rent.commons.entities.Rental;

@RequestMapping(ApiPathConstants.V1_ROUTE + "/rentals")
public interface RentalApi {
	@PostMapping
	ResponseEntity<Rental>createRental(@RequestBody Rental rental);
	
	@GetMapping
	ResponseEntity<List<Rental>>getAllRentals();
	
	@GetMapping("/{id}")
	ResponseEntity<Rental>getRentalById(@PathVariable Long id, @RequestBody Rental rental);
	
    @PutMapping("/{id}")
	ResponseEntity<Rental> updateRental(@PathVariable Long id, @RequestBody Rental rental);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteRental(@PathVariable Long id);
	

}
