package com.alquiler.car_rent.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/v1/reports")
public interface JasperReportApi {
	
	@GetMapping("/monthly-rentals-pdf")
	ResponseEntity<byte[]>getMonthlyRentalReports();
	

}
