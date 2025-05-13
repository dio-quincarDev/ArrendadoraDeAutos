// src/main/java/com/alquiler/car_rent/service/impl/reportsImpl/ReportingServiceImpl.java
package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.service.reportService.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportingServiceImpl implements ReportingService {

    private static final Logger logger = LoggerFactory.getLogger(ReportingServiceImpl.class);

    private final ReportDataService reportDataService;
    private final PdfReportService pdfReportService;
    private final ExcelReportService excelReportService;
    private final JsonReportService jsonReportService;
    private final ChartReportService chartReportService;

    public ReportingServiceImpl(
            ReportDataService reportDataService,
            PdfReportService pdfReportService,
            ExcelReportService excelReportService,
            JsonReportService jsonReportService,
            ChartReportService chartReportService
    ) {
        this.reportDataService = reportDataService;
        this.pdfReportService = pdfReportService;
        this.excelReportService = excelReportService;
        this.jsonReportService = jsonReportService;
        this.chartReportService = chartReportService;
    }

    public Map<String, Object> generateReportData(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate) {
        LocalDate resolvedStart;
        LocalDate resolvedEnd;

        if (period != null) {
            resolvedStart = Optional.ofNullable(startDate).orElse(LocalDate.now().minus(period.getValue(), period.getUnit()));
            resolvedEnd = Optional.ofNullable(endDate).orElse(LocalDate.now());
        } else {
            resolvedStart = Optional.ofNullable(startDate).orElse(LocalDate.now().minusMonths(1)); // Lógica por defecto si period es null
            resolvedEnd = Optional.ofNullable(endDate).orElse(LocalDate.now());
        }
        return reportDataService.generateReportData(period, resolvedStart, resolvedEnd);
    }


    @Override
    public byte[] generateReport(ReportingConstants.OutputFormat format,
                                 ReportingConstants.ReportType reportType,
                                 ReportingConstants.TimePeriod period,
                                 LocalDate startDate,
                                 LocalDate endDate) {
        try {
            Map<String, Object> reportData = generateReportData(period, startDate, endDate);
            return switch (format) {
                case PDF -> pdfReportService.generateReport(reportData, reportType, format);
                case EXCEL -> excelReportService.generateReport(reportData, reportType, format);
                case JSON -> jsonReportService.generateReport(reportData, reportType, format);
                case CHART_PNG -> chartReportService.generateChartAsPng(reportData, reportType);
                case CHART_SVG -> chartReportService.generateChartAsSvg(reportData, reportType);
                default -> throw new IllegalArgumentException("Formato no soportado: " + format);
            };
        } catch (Exception e) {
            logger.error("Error al generar reporte [{} - {} - {}]: {}", format, reportType, period, e.getMessage(), e);
            throw new RuntimeException("Error al generar reporte: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getRentalTrends(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate) {
        return reportDataService.generateReportData(period, startDate, endDate)
                .getOrDefault("rentalTrends", Collections.emptyList()) instanceof List list ? list : List.of();
    }

    @Override
    public Map<Vehicle, Long> getVehicleUsage(LocalDate startDate, LocalDate endDate) {
        return reportDataService.getRentalsInRange(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay()).stream()
                .collect(Collectors.groupingBy(
                        Rental::getVehicle,
                        Collectors.counting()
                ));
    }

    @Override
    public byte[] generateGenericTableExcel(List<String> headers, List<List<String>> data) {
        return excelReportService.generateGenericTableExcel(headers, data);
    }

    // Eliminar estos métodos si ya no se usan directamente desde otro lado
    @Override
    public long getTotalRentals(LocalDate startDate, LocalDate endDate) {
        throw new UnsupportedOperationException("Usar generateReportData() para obtener métricas");
    }

    @Override
    public double getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        throw new UnsupportedOperationException("Usar generateReportData() para obtener métricas");
    }

    @Override
    public long getUniqueVehiclesRented(LocalDate startDate, LocalDate endDate) {
        throw new UnsupportedOperationException("Usar generateReportData() para obtener métricas");
    }

    @Override
    public Map<String, Object> getMostRentedVehicle(LocalDate startDate, LocalDate endDate) {
        throw new UnsupportedOperationException("Usar generateReportData() para obtener métricas");
    }

    @Override
    public long getNewCustomersCount(LocalDate startDate, LocalDate endDate) {
        throw new UnsupportedOperationException("Usar generateReportData() para obtener métricas");
    }
}
