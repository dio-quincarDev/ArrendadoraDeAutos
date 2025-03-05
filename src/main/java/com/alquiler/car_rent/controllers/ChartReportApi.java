package com.alquiler.car_rent.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;

@RequestMapping(ApiPathConstants.STATISTICS_BASE_PATH)
public interface ChartReportApi {
	@GetMapping("/most-rented-cars")
	ResponseEntity<byte[]> getMostRentedCarChart();
	
	@GetMapping("/most-rented-cars/download")
	ResponseEntity<byte[]> downloadMostRentedCarChart();
}
