package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.service.reportService.*;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ReportFormatServiceImpl implements ReportFormatService {
    private final Map<ReportingConstants.OutputFormat, ReportFormatService> formatServices;

    public ReportFormatServiceImpl(
            PdfReportService pdfReportService,
            ExcelReportService excelReportService,
            JsonReportService jsonReportService,
            ChartReportService chartReportService) {

        this.formatServices = Map.of(
                ReportingConstants.OutputFormat.PDF, pdfReportService,
                ReportingConstants.OutputFormat.EXCEL, excelReportService,
                ReportingConstants.OutputFormat.JSON, jsonReportService,
                ReportingConstants.OutputFormat.CHART_PNG, chartReportService,
                ReportingConstants.OutputFormat.CHART_SVG, chartReportService
        );
    }

    @Override
    public byte[] generateReport(Map<String, Object> data, ReportingConstants.ReportType reportType, ReportingConstants.OutputFormat format) {
        ReportFormatService service = formatServices.get(format);
        if (service == null) {
            throw new UnsupportedOperationException("Formato no soportado: " + format);
        }
        return service.generateReport(data, reportType, format);
    }

    @Override
    public String getReportTitle(ReportingConstants.ReportType reportType) {
        return reportType.getTitle(); // Usamos el título del enum
    }
}