package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.service.reportService.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.List;

@Service
public class ReportingServiceImpl implements ReportingService {

    private static final Logger logger = LoggerFactory.getLogger(ReportingServiceImpl.class);

    private final ReportDataService reportDataService;
    private final MetricsService metricsService;
    private final PdfReportService pdfReportService;
    private final ExcelReportService excelReportService;
    private final JsonReportService jsonReportService;
    private final ChartReportService chartReportService;

    public ReportingServiceImpl(
            ReportDataService reportDataService,
            MetricsService metricsService,
            PdfReportService pdfReportService,
            ExcelReportService excelReportService,
            JsonReportService jsonReportService,
            ChartReportService chartReportService
    ) {
        this.reportDataService = reportDataService;
        this.metricsService = metricsService;
        this.pdfReportService = pdfReportService;
        this.excelReportService = excelReportService;
        this.jsonReportService = jsonReportService;
        this.chartReportService = chartReportService;
    }

    @Override
    public Map<String, Object> generateReportData(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate) {
        return reportDataService.generateReportData(period, startDate, endDate);
    }

    @Override
    public byte[] generateReport(ReportingConstants.OutputFormat format, ReportingConstants.ReportType reportType, ReportingConstants.TimePeriod period,
                                 LocalDate startDate, LocalDate endDate) {
        try {
            Map<String, Object> reportData;
            if (reportType == ReportingConstants.ReportType.GENERIC_METRICS) {
                LocalDate[] range = resolveDateRange(startDate, endDate);
                LocalDate start = range[0], end = range[1];

                reportData = new HashMap<>();
                reportData.put("totalRentals", metricsService.getTotalRentals(start, end));
                reportData.put("totalRevenue", metricsService.getTotalRevenue(start, end));
                reportData.put("uniqueVehicles", metricsService.getUniqueVehiclesRented(start, end));
                reportData.put("mostRentedVehicle", metricsService.getMostRentedVehicle(start, end));
                reportData.put("newCustomers", metricsService.getNewCustomersCount(start, end));
            } else {
                reportData = generateReportData(period, startDate, endDate);
            }

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

    private LocalDate[] resolveDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate defaultStart = LocalDate.now().minusMonths(1);
        LocalDate defaultEnd = LocalDate.now();
        return new LocalDate[] {
                Optional.ofNullable(startDate).orElse(defaultStart),
                Optional.ofNullable(endDate).orElse(defaultEnd)
        };
    }

    @Override
    public long getTotalRentals(LocalDate startDate, LocalDate endDate) {
        return metricsService.getTotalRentals(startDate, endDate);
    }

    @Override
    public double getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        return metricsService.getTotalRevenue(startDate, endDate);
    }

    @Override
    public long getUniqueVehiclesRented(LocalDate startDate, LocalDate endDate) {
        return metricsService.getUniqueVehiclesRented(startDate, endDate);
    }

    @Override
    public Map<String, Object> getMostRentedVehicle(LocalDate startDate, LocalDate endDate) {
        return metricsService.getMostRentedVehicle(startDate, endDate);
    }

    @Override
    public long getNewCustomersCount(LocalDate startDate, LocalDate endDate) {
        return metricsService.getNewCustomersCount(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getRentalTrends(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate) {
        return metricsService.getRentalTrends(period, startDate, endDate);
    }

    @Override
    public Map<Vehicle, Long> getVehicleUsage(LocalDate startDate, LocalDate endDate) {
        return metricsService.getVehicleUsage(startDate, endDate);
    }

    @Override
    public byte[] generateGenericTableExcel(List<String> headers, List<List<String>> data) {
        return excelReportService.generateGenericTableExcel(headers, data);
    }
}
