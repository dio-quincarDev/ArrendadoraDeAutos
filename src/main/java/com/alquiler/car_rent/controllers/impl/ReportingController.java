package com.alquiler.car_rent.controllers.impl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.dtos.ExportMetricsRequest;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.controllers.ReportingApi;
import com.alquiler.car_rent.service.reportService.ReportingService;
import org.springframework.http.*;
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
    public ResponseEntity<Map<String, Object>> getDashboardData(ReportingConstants.TimePeriod period,
                                                                LocalDate startDate,
                                                                LocalDate endDate) {
        return ResponseEntity.ok(reportingService.generateReportData(period, startDate, endDate));
    }

    @Override
    public ResponseEntity<byte[]> exportReport(ReportingConstants.OutputFormat format,
                                               ReportingConstants.ReportType reportType,
                                               ReportingConstants.TimePeriod period,
                                               LocalDate startDate,
                                               LocalDate endDate) {

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
                period.name().toLowerCase(),
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
    public ResponseEntity<Long> getTotalRentalsMetric(LocalDate startDate, LocalDate endDate) {
        return ResponseEntity.ok(reportingService.getTotalRentals(startDate, endDate));
    }

    @Override
    public ResponseEntity<Double> getTotalRevenueMetric(LocalDate startDate, LocalDate endDate) {
        return ResponseEntity.ok(reportingService.getTotalRevenue(startDate, endDate));
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
    public ResponseEntity<List<Map<String, Object>>> getRentalTrendsMetric(ReportingConstants.TimePeriod period,
                                                                           LocalDate startDate,
                                                                           LocalDate endDate) {
        return ResponseEntity.ok(reportingService.getRentalTrends(period, startDate, endDate));
    }

    @Override
    public ResponseEntity<Map<Vehicle, Long>> getVehicleUsageMetric(LocalDate startDate, LocalDate endDate) {
        return ResponseEntity.ok(reportingService.getVehicleUsage(startDate, endDate));
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
}
