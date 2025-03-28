package com.alquiler.car_rent.controllers;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.alquiler.car_rent.service.ReportingService.OutputFormat;
import com.alquiler.car_rent.service.ReportingService.ReportType;
import com.alquiler.car_rent.service.ReportingService.TimePeriod;
import java.time.LocalDate;

@RequestMapping(ApiPathConstants.REPORTS_BASE_PATH + "/reports")
public interface ReportingApi {

    @GetMapping(produces = "text/html")
    String viewReport(
        @RequestParam(defaultValue = "MONTHLY") TimePeriod period,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        Model model
    );

    @GetMapping("/export")
    ResponseEntity<byte[]> exportReport(
        @RequestParam(defaultValue = "PDF") OutputFormat format,
        @RequestParam(defaultValue = "RENTAL_SUMMARY") ReportType reportType,
        @RequestParam(defaultValue = "MONTHLY") TimePeriod period,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );
}