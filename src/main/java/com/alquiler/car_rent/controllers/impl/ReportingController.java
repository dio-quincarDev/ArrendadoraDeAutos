package com.alquiler.car_rent.controllers.impl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.dtos.ExportMetricsRequest;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.controllers.ReportingApi;
import com.alquiler.car_rent.service.reportService.ReportingService;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
public class ReportingController implements ReportingApi {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService){
        this.reportingService = reportingService;
    }

    @Override
    public ResponseEntity<Map<String, Object>> getDashboardData(
            ReportingConstants.TimePeriod period,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Map<String, Object> dashboardData = reportingService.generateReportData(period, startDate, endDate);
        return ResponseEntity.ok(dashboardData);
    }

    @Override
    public ResponseEntity<byte[]> exportReport(
            ReportingConstants.OutputFormat format,
            ReportingConstants.ReportType reportType,
            ReportingConstants.TimePeriod period,
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
            ReportingConstants.OutputFormat format,
            ReportingConstants.ReportType reportType,
            ReportingConstants.TimePeriod period,
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
                headers.setContentDisposition(
                        ContentDisposition.attachment() // Or inline if you want to display in browser
                                .filename(filename + ".json")
                                .build()
                );
                break;
            case CHART_PNG:
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentDisposition(
                        ContentDisposition.inline()
                                .filename(filename + ".png")
                                .build()
                );
                break;
            case CHART_SVG:
                headers.setContentType(MediaType.parseMediaType("image/svg+xml"));
                headers.setContentDisposition(
                        ContentDisposition.inline()
                                .filename(filename + ".svg")
                                .build()
                );
                break;
            case EXCEL:
                headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                headers.setContentDisposition(
                        ContentDisposition.attachment()
                                .filename(filename + ".xlsx")
                                .build()
                );
                break;
        }
        return headers;
    }

    private String generateFilename(
            ReportingConstants.ReportType reportType,
            ReportingConstants.TimePeriod period,
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

    @Override
    public ResponseEntity<Long> getTotalRentalsMetric(LocalDate startDate, LocalDate endDate) {
        return ResponseEntity.ok(reportingService.getTotalRentals(startDate, endDate));
    }

    @Override
    public ResponseEntity<Double> getTotalRevenueMetric(LocalDate startDate, LocalDate endDate) {
        Double revenue = reportingService.getTotalRevenue(startDate, endDate);
        return ResponseEntity.ok(revenue != null ? revenue : 0.0);
    }

    @Override
    public ResponseEntity<Long> getUniqueVehiclesRentedMetric(LocalDate startDate, LocalDate endDate) {
        return ResponseEntity.ok(reportingService.getUniqueVehiclesRented(startDate, endDate));
    }

    @Override
    public ResponseEntity<Map<String, Object>> getMostRentedVehicleMetric(LocalDate startDate, LocalDate endDate) {
        return ResponseEntity.ok(reportingService.getMostRentedVehicle(startDate, endDate));
    }

    @Override
    public ResponseEntity<Long> getNewCustomersCountMetric(LocalDate startDate, LocalDate endDate) {
        return ResponseEntity.ok(reportingService.getNewCustomersCount(startDate, endDate));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> getRentalTrendsMetric(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate) {
        return ResponseEntity.ok(reportingService.getRentalTrends(period, startDate, endDate));
    }

    @Override
    public ResponseEntity<Map<Vehicle, Long>> getVehicleUsageMetric(LocalDate startDate, LocalDate endDate) {
        return ResponseEntity.ok(reportingService.getVehicleUsage(startDate, endDate));
    }


    @Override
    public ResponseEntity<byte[]> exportMetrics(ExportMetricsRequest request) {
        byte[] fileBytes;

        if ("EXCEL".equalsIgnoreCase(request.getFormat())) {
            fileBytes = reportingService.generateGenericTableExcel(
                    request.getHeaders(), request.getData()
            );
        } else {
            throw new UnsupportedOperationException("Formato no soportado: " + request.getFormat());
        }

        String fileName = "metricas_" + LocalDate.now() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(fileBytes);
    }
}