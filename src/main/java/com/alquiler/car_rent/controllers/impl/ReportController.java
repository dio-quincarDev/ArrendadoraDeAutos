package com.alquiler.car_rent.controllers.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.alquiler.car_rent.controllers.ReportApi;
import com.alquiler.car_rent.service.JasperReportService;
import com.alquiler.car_rent.service.ReportService;

@RestController
public class ReportController implements ReportApi {
	
	private final ReportService reportService;
	private final JasperReportService jasperReportService;
	
	public ReportController(ReportService reportService, JasperReportService jasperReportService) {
		this.reportService = reportService;
		this.jasperReportService =jasperReportService;
	}

    @Override
    public ResponseEntity<byte[]> getMostRentedCarChart() {
        try {
            File chartFile = reportService.generatedMostRentedCarChart();
            byte[] imageBytes = Files.readAllBytes(chartFile.toPath());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "image/png");

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    public ResponseEntity<byte[]> getMonthlyRentalReport() {
        try {
            File reportFile = jasperReportService.generateMonthlyRentalReport();
            byte[] pdfBytes = Files.readAllBytes(reportFile.toPath());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/pdf");
            headers.set("Content-Disposition", "attachment; filename=rental_report.pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        
        }
        
    }
	

}
