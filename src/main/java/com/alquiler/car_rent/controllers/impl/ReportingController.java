package com.alquiler.car_rent.controllers.impl;

import com.alquiler.car_rent.commons.dtos.ExportMetricsRequest;
import com.alquiler.car_rent.controllers.ReportingApi;
import com.alquiler.car_rent.service.reportService.ReportingService;
import com.alquiler.car_rent.service.reportService.ReportingService.OutputFormat;
import com.alquiler.car_rent.service.reportService.ReportingService.ReportType;
import com.alquiler.car_rent.service.reportService.ReportingService.TimePeriod;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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

    @Override
    public ResponseEntity<Long> getTotalRentalsMetric(
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate
    ) {
        long totalRentals = reportingService.getTotalRentals(startDate, endDate);
        return ResponseEntity.ok(totalRentals);
    }

    @Override
    public ResponseEntity<Double> getTotalRevenueMetric(
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate
    ) {
        double totalRevenue = reportingService.getTotalRevenue(startDate, endDate);
        return ResponseEntity.ok(totalRevenue);
    }

    @Override
    public ResponseEntity<Long> getUniqueVehiclesRentedMetric(
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate
    ) {
        long uniqueVehicles = reportingService.getUniqueVehiclesRented(startDate, endDate);
        return ResponseEntity.ok(uniqueVehicles);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getMostRentedVehicleMetric(
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate
    ) {
        Map<String, Object> mostRented = reportingService.getMostRentedVehicle(startDate, endDate);
        return ResponseEntity.ok(mostRented);
    }

    @Override
    public ResponseEntity<Long> getNewCustomersCountMetric(
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate
    ) {
        long newCustomers = reportingService.getNewCustomersCount(startDate, endDate);
        return ResponseEntity.ok(newCustomers);
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> getRentalTrendsMetric(
            @RequestParam(value = "period", required = false) TimePeriod period,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate
    ) {
        List<Map<String, Object>> rentalTrends = reportingService.getRentalTrends(period, startDate, endDate);
        return ResponseEntity.ok(rentalTrends);
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