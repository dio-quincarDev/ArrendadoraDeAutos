package com.alquiler.car_rent.controllers;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;
import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.dtos.ExportMetricsRequest;
import com.alquiler.car_rent.commons.entities.Vehicle;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequestMapping( ApiPathConstants.REPORTS_BASE_PATH)
public interface ReportingApi {

    @GetMapping(produces = "application/json") // Modified to produce JSON
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Map<String, Object>> getDashboardData( // Renamed for clarity
                                                          @RequestParam(defaultValue = "MONTHLY") ReportingConstants.TimePeriod period,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<byte[]> exportReport(
            @RequestParam(defaultValue = "PDF") ReportingConstants.OutputFormat format,
            @RequestParam(defaultValue = "RENTAL_SUMMARY") ReportingConstants.ReportType reportType,
            @RequestParam(defaultValue = "MONTHLY") ReportingConstants.TimePeriod period,
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
            @RequestParam(value = "period", required = false) ReportingConstants.TimePeriod period,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    // Ejemplo de endpoint adicional para el dashboard
    @GetMapping("/metrics/vehicle-usage")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Map<Vehicle, Long>> getVehicleUsageMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    @PostMapping("/export-metrics")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<byte[]> exportMetrics(@RequestBody ExportMetricsRequest request);
}