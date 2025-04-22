package com.alquiler.car_rent.service.reportService;

import com.alquiler.car_rent.commons.constants.ReportingConstants;

import java.util.List;
import java.util.Map;

public interface ExcelReportGenericTable {
    byte[] generateReport(Map<String, Object> data, ReportingConstants.ReportType reportType, ReportingConstants.OutputFormat format);

    String getReportTitle(ReportingConstants.ReportType reportType);

    byte[] generateGenericTableExcel(List<String> headers, List<List<String>> data); // Nuevo método
}
