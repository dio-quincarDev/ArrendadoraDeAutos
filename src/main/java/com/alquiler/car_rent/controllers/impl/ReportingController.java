package com.alquiler.car_rent.controllers.impl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.dtos.ExportMetricsRequest;
import com.alquiler.car_rent.controllers.ReportingApi;
import com.alquiler.car_rent.service.reportService.ReportingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ReportingController implements ReportingApi {

    private final ReportingService reportingService;
    private final ObjectMapper objectMapper; // Inject ObjectMapper

    public ReportingController(ReportingService reportingService, ObjectMapper objectMapper) {
        this.reportingService = reportingService;
        this.objectMapper = objectMapper;
    }

    private Map<String, Object> getGenericReportData(LocalDate startDate, LocalDate endDate, ReportingConstants.TimePeriod period) {
        byte[] reportBytes = reportingService.generateReport(
                ReportingConstants.OutputFormat.JSON,
                ReportingConstants.ReportType.GENERIC_METRICS,
                period, // Using the passed period
                startDate,
                endDate
        );
        try {
            return objectMapper.readValue(reportBytes, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getDashboardData(
            ReportingConstants.TimePeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate safeStart = (startDate != null && startDate.isAfter(LocalDate.of(2024, 1, 1)))
                ? startDate : LocalDate.of(1900, 1, 1);
        LocalDate safeEnd = (endDate != null && endDate.isBefore(LocalDate.of(2035, 1, 1)))
                ? endDate : LocalDate.of(2100, 1, 1);

        return ResponseEntity.ok(reportingService.generateReportData(period, safeStart, safeEnd));
    }


    @Override
    public ResponseEntity<byte[]> exportReport(ReportingConstants.OutputFormat format,
                                               ReportingConstants.ReportType reportType,
                                               ReportingConstants.TimePeriod period,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        byte[] report = reportingService.generateReport(format, reportType, period, startDate, endDate);

        return ResponseEntity.ok()
                .headers(buildExportHeaders(format, reportType, period, startDate, endDate))
                .body(report);
    }

    private HttpHeaders buildExportHeaders(ReportingConstants.OutputFormat format,
                                           ReportingConstants.ReportType reportType,
                                           ReportingConstants.TimePeriod period,
                                           LocalDate startDate,
                                           LocalDate endDate) {
        String filename = generateFilename(reportType, period, startDate, endDate);
        MediaType mediaType = getMediaType(format);
        String fileExtension = getFileExtension(format);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(ContentDisposition.builder(mediaType.equals(MediaType.APPLICATION_PDF) ? "attachment" : "inline")
                .filename(filename + fileExtension).build());
        return headers;
    }

    private String generateFilename(ReportingConstants.ReportType reportType,
                                    ReportingConstants.TimePeriod period,
                                    LocalDate startDate,
                                    LocalDate endDate) {
        String dateRange = (startDate != null && endDate != null)
                ? String.format("%s_%s", startDate, endDate)
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("%s_%s_%s",
                reportType.name().toLowerCase(),
                period != null ? period.name().toLowerCase() : "alltime", // Manejar null por si acaso
                dateRange);
    }

    private MediaType getMediaType(ReportingConstants.OutputFormat format) {
        return switch (format) {
            case PDF -> MediaType.APPLICATION_PDF;
            case HTML -> null;
            case JSON -> MediaType.APPLICATION_JSON;
            case CHART_PNG -> MediaType.IMAGE_PNG;
            case CHART_SVG -> MediaType.parseMediaType("image/svg+xml");
            case EXCEL -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        };
    }

    private String getFileExtension(ReportingConstants.OutputFormat format) {
        return switch (format) {
            case PDF -> ".pdf";
            case HTML -> null;
            case JSON -> ".json";
            case CHART_PNG -> ".png";
            case CHART_SVG -> ".svg";
            case EXCEL -> ".xlsx";
        };
    }

    @Override
    public ResponseEntity<Long> getTotalRentalsMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "MONTHLY") ReportingConstants.TimePeriod period
    ) {
        Map<String, Object> reportData = getGenericReportData(startDate, endDate, period);
        Long totalRentals = ((Number) reportData.get("totalRentals")).longValue();
        return ResponseEntity.ok(totalRentals);
    }

    @Override
    public ResponseEntity<Double> getTotalRevenueMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "MONTHLY") ReportingConstants.TimePeriod period
    ) {
        Map<String, Object> reportData = getGenericReportData(startDate, endDate, period);
        Double totalRevenue = (Double) reportData.get("totalRevenue");
        return ResponseEntity.ok(totalRevenue);
    }

    @Override
    public ResponseEntity<Long> getUniqueVehiclesRentedMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "MONTHLY") ReportingConstants.TimePeriod period
    ) {
        Map<String, Object> reportData = getGenericReportData(startDate, endDate, period);
        Long uniqueVehicles = (Long) reportData.get("uniqueVehicles");
        return ResponseEntity.ok(uniqueVehicles);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getMostRentedVehicleMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "MONTHLY") ReportingConstants.TimePeriod period
    ) {
        Map<String, Object> reportData = getGenericReportData(startDate, endDate, period);
        return ResponseEntity.ok((Map<String, Object>) reportData.get("mostRentedVehicle"));

    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> getVehicleUsageMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "MONTHLY") ReportingConstants.TimePeriod period
    ) {
        Map<String, Object> reportData = getGenericReportData(startDate, endDate, period);
        List<Map<String, Object>> usageList = (List<Map<String, Object>>) reportData.get("vehicleUsage");

        List<Map<String, Object>> formatted = usageList.stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("brandModel", entry.get("vehicle"));
                    map.put("usageCount", entry.get("count"));
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(formatted);
    }


    @Override
    public ResponseEntity<Long> getNewCustomersCountMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "MONTHLY") ReportingConstants.TimePeriod period
    ) {
        Map<String, Object> reportData = getGenericReportData(startDate, endDate, period);
        Long newCustomers = ((Number) reportData.get("newCustomers")).longValue();
        return ResponseEntity.ok(newCustomers);
    }


    public ResponseEntity<List<Map<String, Object>>> getRentalTrendsMetric(
            @RequestParam(value = "period", required = false) ReportingConstants.TimePeriod period,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<Map<String, Object>> rentalTrends =
                (List<Map<String, Object>>) reportingService
                        .generateReportData(period, startDate, endDate)
                        .getOrDefault("rentalTrends", List.of());
        return ResponseEntity.ok(rentalTrends);
    }


    @Override
    public ResponseEntity<byte[]> exportMetrics(ExportMetricsRequest request) {
        if (!"EXCEL".equalsIgnoreCase(request.getFormat())) {
            throw new UnsupportedOperationException("Formato no soportado: " + request.getFormat());
        }

        byte[] bytes = reportingService.generateGenericTableExcel(request.getHeaders(), request.getData());
        String fileName = "metricas_" + LocalDate.now() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> getAverageRentalDurationMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "MONTHLY") ReportingConstants.TimePeriod period
    ) {
        Map<String, Object> reportData = getGenericReportData(startDate, endDate, period);
        Map<String, Double> avgMap = (Map<String, Double>) reportData.get("averageRentalDurationByTopCustomers");

        List<Map<String, Object>> formatted = avgMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("customer", e.getKey());
                    map.put("averageDuration", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(formatted);
    }



    @Override
    public ResponseEntity<List<Map<String, Object>>> getTopCustomersMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "MONTHLY") ReportingConstants.TimePeriod period
    ) {
        Map<String, Object> reportData = getGenericReportData(startDate, endDate, period);
        List<Map<String, Object>> topCustomers = (List<Map<String, Object>>) reportData.get("topCustomersByRentals");
        return ResponseEntity.ok(topCustomers);
    }
}