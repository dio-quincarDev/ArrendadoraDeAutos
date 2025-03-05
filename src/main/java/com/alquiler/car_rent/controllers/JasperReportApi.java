package com.alquiler.car_rent.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;

@RequestMapping(ApiPathConstants.REPORTS_BASE_PATH)
public interface JasperReportApi {

    @GetMapping("/monthly-rentals")
    ResponseEntity<byte[]> getMonthlyRentalReports();

    @GetMapping("/monthly-rentals/download")
    ResponseEntity<byte[]> downloadMonthlyRentalReport();
}
