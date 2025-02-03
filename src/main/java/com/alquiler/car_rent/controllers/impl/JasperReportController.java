package com.alquiler.car_rent.controllers.impl;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.alquiler.car_rent.controllers.JasperReportApi;
import com.alquiler.car_rent.service.JasperReportService;

@RestController
public class JasperReportController implements JasperReportApi{
	private final JasperReportService jasperReportService;
	
	public JasperReportController(JasperReportService jasperReportService) {
		this.jasperReportService = jasperReportService;
	}

	@Override
	public ResponseEntity<byte[]> getMonthlyRentalReports() {
		try {
			File reportFile = jasperReportService.generateMonthlyRentalReport();
			byte[]pdfBytes = Files.readAllBytes(reportFile.toPath());
			
			HttpHeaders headers =  new HttpHeaders();
			
			headers.set("Content-Type", "application/pdf");
	        headers.set("Content-Disposition", "attachment; filename=rental_report.pdf");

	            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
		}catch(IOException e) {
			
			 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);		
		}
	
	}

}
