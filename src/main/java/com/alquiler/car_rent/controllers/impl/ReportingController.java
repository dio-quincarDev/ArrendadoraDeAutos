package com.alquiler.car_rent.controllers.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RestController;

import com.alquiler.car_rent.controllers.ReportingApi;
import com.alquiler.car_rent.service.ReportingService;
import com.alquiler.car_rent.service.ReportingService.OutputFormat;
import com.alquiler.car_rent.service.ReportingService.ReportType;
import com.alquiler.car_rent.service.ReportingService.TimePeriod;

@RestController
public class ReportingController implements ReportingApi {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @Override
    public String viewReport(
        TimePeriod period,
        LocalDate startDate,
        LocalDate endDate,
        Model model
    ) {
        model.addAttribute("reportData", 
            reportingService.generateReportData(period, startDate, endDate)
        );
        model.addAttribute("periods", TimePeriod.values());
        return "report";
    }

    @Override
    public ResponseEntity<byte[]> exportReport(
        OutputFormat format,
        ReportType reportType,
        TimePeriod period,
        LocalDate startDate,
        LocalDate endDate
    ) {
        byte[] reportBytes = reportingService.generateReport(
            format, reportType, period, startDate, endDate
        );

        return ResponseEntity.ok()
            .headers(createHeaders(format, reportType, period, startDate, endDate))
            .body(reportBytes);
    }

    private HttpHeaders createHeaders(
        OutputFormat format,
        ReportType reportType,
        TimePeriod period,
        LocalDate startDate,
        LocalDate endDate
    ) {
        HttpHeaders headers = new HttpHeaders();
        String filename = generateFilename(reportType, period, startDate, endDate);

        switch (format) {
            case PDF:
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDisposition(
                    ContentDisposition.attachment()
                        .filename(filename + ".pdf")
                        .build()
                );
                break;
            case JSON:
                headers.setContentType(MediaType.APPLICATION_JSON);
                break;
            case CHART_PNG:
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentDisposition(
                    ContentDisposition.inline()
                        .filename(filename + ".png")
                        .build()
                );
                break;
            // Add other formats as needed
        }

        return headers;
    }

    private String generateFilename(
        ReportType reportType,
        TimePeriod period,
        LocalDate startDate,
        LocalDate endDate
    ) {
        String dateRange = (startDate != null && endDate != null)
            ? startDate.format(DateTimeFormatter.ISO_DATE) + "_" + endDate.format(DateTimeFormatter.ISO_DATE)
            : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return String.format("%s_%s_%s",
            reportType.name().toLowerCase(),
            period.name().toLowerCase(),
            dateRange
        );
    }
}