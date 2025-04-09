package com.alquiler.car_rent.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	ResponseEntity<RentalDto>createRental(@RequestBody RentalDto rentalDto);
	
	@GetMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	ResponseEntity<List<RentalDto>>getAllRentals();
	
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	ResponseEntity<RentalDto>getRentalById(@PathVariable Long id);
	
    @PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	ResponseEntity<RentalDto> updateRental(@PathVariable Long id, @RequestBody RentalDto rentalDto);
    
    @PutMapping("/{id}/cancel")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    ResponseEntity<RentalDto> cancelRental(@PathVariable Long id);

    @DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    ResponseEntity<Void> deleteRental(@PathVariable Long id);
	

}
