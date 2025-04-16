package com.alquiler.car_rent.controllers;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;
import com.alquiler.car_rent.service.ReportingService.OutputFormat;
import com.alquiler.car_rent.service.ReportingService.ReportType;
import com.alquiler.car_rent.service.ReportingService.TimePeriod;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequestMapping(ApiPathConstants.V1_ROUTE + ApiPathConstants.REPORTS_BASE_PATH)
public interface ReportingApi {

    @GetMapping(produces = "text/html")
    @PreAuthorize("hasRole('ADMIN')")
    String viewReport(
            @RequestParam(defaultValue = "MONTHLY") TimePeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    );

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<byte[]> exportReport(
            @RequestParam(defaultValue = "PDF") OutputFormat format,
            @RequestParam(defaultValue = "RENTAL_SUMMARY") ReportType reportType,
            @RequestParam(defaultValue = "MONTHLY") TimePeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    @GetMapping("/metrics/total-rentals")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Long> getTotalRentalsMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    @GetMapping("/metrics/total-revenue")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Double> getTotalRevenueMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    @GetMapping("/metrics/unique-vehicles")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Long> getUniqueVehiclesRentedMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    @GetMapping("/metrics/most-rented-vehicle")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Map<String, Object>> getMostRentedVehicleMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    @GetMapping("/metrics/new-customers")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Long> getNewCustomersCountMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    @GetMapping("/metrics/rental-trends")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<List<Map<String, Object>>> getRentalTrendsMetric(
            @RequestParam(value = "period", required = false) TimePeriod period,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

   
}