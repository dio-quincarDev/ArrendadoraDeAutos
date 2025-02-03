package com.alquiler.car_rent.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/v1/reports")
public interface ReportApi {
	
	@GetMapping("/most-rented-cars")
	ResponseEntity<byte[]>getMostRentedCarChart();
	
	@GetMapping("/monthly-rentals")
	ResponseEntity<byte[]>getMonthlyRentalReport();

}
