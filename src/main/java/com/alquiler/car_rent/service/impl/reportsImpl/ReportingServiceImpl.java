package com.alquiler.car_rent.service.impl.reportsImpl;


import com.alquiler.car_rent.commons.constants.ReportingConstants;
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
            Map<String, Object> reportData = generateReportData(period, startDate, endDate);

            switch (format) {
                case PDF:
                    return pdfReportService.generateReport(reportData, reportType);
                case EXCEL:
                    return excelReportService.generateReport(reportData, reportType);
                case JSON:
                    return jsonReportService.generateReport(reportData, reportType, format);
                case CHART_PNG:
                    return chartReportService.generateChartAsPng(reportData, reportType);
                case CHART_SVG:
                    return chartReportService.generateChartAsSvg(reportData, reportType);
                default:
                    throw new IllegalArgumentException("Formato no soportado: " + format);
            }
        } catch (Exception e) {
            logger.error("Error al generar reporte: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar reporte: " + e.getMessage(), e);
        }
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
    public byte[] generateGenericTableExcel(List<String> headers, List<List<String>> data) {
        return excelReportService.generateGenericTableExcel(headers, data);
    }
}