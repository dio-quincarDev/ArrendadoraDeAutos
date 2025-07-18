package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.service.reportService.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ReportingServiceImpl implements ReportingService {

    private static final Logger logger = LoggerFactory.getLogger(ReportingServiceImpl.class);

    private final ReportDataService reportDataService;
    private final PdfReportService pdfReportService;
    private final ExcelReportService excelReportService;
    private final JsonReportService jsonReportService;
    private final ChartReportService chartReportService;
    private final MetricsService metricsService;

    public ReportingServiceImpl(
            ReportDataService reportDataService,
            PdfReportService pdfReportService,
            ExcelReportService excelReportService,
            JsonReportService jsonReportService,
            ChartReportService chartReportService,
            MetricsService metricsService
    ) {
        this.reportDataService = reportDataService;
        this.pdfReportService = pdfReportService;
        this.excelReportService = excelReportService;
        this.jsonReportService = jsonReportService;
        this.chartReportService = chartReportService;
        this.metricsService = metricsService;
    }

    public Map<String, Object> generateReportData(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate) {
        LocalDate resolvedStart;
        LocalDate resolvedEnd;

        if (period == ReportingConstants.TimePeriod.ALL_TIME) {
            resolvedStart = Optional.ofNullable(startDate).orElse(LocalDate.MIN); // O una fecha muy antigua
            resolvedEnd = Optional.ofNullable(endDate).orElse(LocalDate.MAX);   // O una fecha muy futura
        } else {
            resolvedStart = Optional.ofNullable(startDate).orElse(LocalDate.now().minus(period.getValue(), period.getUnit()));
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
        return metricsService.getVehicleUsage(ReportingConstants.TimePeriod.ALL_TIME, startDate, endDate);
    }

    @Override
    public byte[] generateGenericTableExcel(List<String> headers, List<List<String>> data) {
        return excelReportService.generateGenericTableExcel(headers, data);
    }

    @Override
    public long getTotalRentals(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = generateReportData(ReportingConstants.TimePeriod.ALL_TIME, startDate, endDate);
        return (long) reportData.getOrDefault("totalRentals", 0L);
    }

    @Override
    public double getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = generateReportData(ReportingConstants.TimePeriod.ALL_TIME, startDate, endDate);
        return (double) reportData.getOrDefault("totalRevenue", 0.0);
    }

    @Override
    public long getUniqueVehiclesRented(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = generateReportData(ReportingConstants.TimePeriod.ALL_TIME, startDate, endDate);
        List<Map<String, Object>> vehicleUsage = (List<Map<String, Object>>) reportData.getOrDefault("vehicleUsage", Collections.emptyList());
        return vehicleUsage.size();
    }

    @Override
    public Map<String, Object> getMostRentedVehicle(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = generateReportData(ReportingConstants.TimePeriod.ALL_TIME, startDate, endDate);
        return (Map<String, Object>) reportData.getOrDefault("mostRentedVehicle", Collections.emptyMap());
    }

    @Override
    public long getNewCustomersCount(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = generateReportData(ReportingConstants.TimePeriod.ALL_TIME, startDate, endDate);
        return (long) reportData.getOrDefault("newCustomers", 0L);
    }

    @Override
    public Map<com.alquiler.car_rent.commons.enums.VehicleType, Long> getRentalsByVehicleType(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = generateReportData(ReportingConstants.TimePeriod.ALL_TIME, startDate, endDate);
        return (Map<com.alquiler.car_rent.commons.enums.VehicleType, Long>) reportData.getOrDefault("rentalsByVehicleType", Collections.emptyMap());
    }

    @Override
    public Map<com.alquiler.car_rent.commons.enums.VehicleType, Double> getRevenueByVehicleType(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = generateReportData(ReportingConstants.TimePeriod.ALL_TIME, startDate, endDate);
        return (Map<com.alquiler.car_rent.commons.enums.VehicleType, Double>) reportData.getOrDefault("revenueByVehicleType", Collections.emptyMap());
    }

    @Override
    public Map<com.alquiler.car_rent.commons.enums.PricingTier, Long> getRentalsByPricingTier(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = generateReportData(ReportingConstants.TimePeriod.ALL_TIME, startDate, endDate);
        return (Map<com.alquiler.car_rent.commons.enums.PricingTier, Long>) reportData.getOrDefault("rentalsByPricingTier", Collections.emptyMap());
    }

    @Override
    public Map<com.alquiler.car_rent.commons.enums.PricingTier, Double> getRevenueByPricingTier(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = generateReportData(ReportingConstants.TimePeriod.ALL_TIME, startDate, endDate);
        return (Map<com.alquiler.car_rent.commons.enums.PricingTier, Double>) reportData.getOrDefault("revenueByPricingTier", Collections.emptyMap());
    }
}
