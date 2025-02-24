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
import com.alquiler.car_rent.commons.dtos.RentalDto;


@RequestMapping(ApiPathConstants.V1_ROUTE + "/rentals")
public interface RentalApi {
	@PostMapping
	ResponseEntity<RentalDto>createRental(@RequestBody RentalDto rentalDto);
	
	@GetMapping
	ResponseEntity<List<RentalDto>>getAllRentals();
	
	@GetMapping("/{id}")
	ResponseEntity<RentalDto>getRentalById(@PathVariable Long id);
	
    @PutMapping("/{id}")
	ResponseEntity<RentalDto> updateRental(@PathVariable Long id, @RequestBody RentalDto rentalDto);
    
    @PutMapping("/{id}/cancel")
    ResponseEntity<RentalDto> cancelRental(@PathVariable Long id);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteRental(@PathVariable Long id);
	

}
