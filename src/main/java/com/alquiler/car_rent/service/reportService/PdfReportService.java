package com.alquiler.car_rent.service.reportService;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.itextpdf.text.Document;

import java.util.Map;

public interface PdfReportService extends ReportFormatService {
    void addPdfHeader(Document doc, ReportingConstants.ReportType type, Map<String,Object> data);

    void addReportContent(Document doc, ReportingConstants.ReportType type, Map<String, Object> data);

    @Override
    byte[] generateReport(Map<String, Object> data, ReportingConstants.ReportType reportType, ReportingConstants.OutputFormat format);
}
