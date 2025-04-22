package com.alquiler.car_rent.service.reportService;

import com.alquiler.car_rent.commons.constants.ReportingConstants;

import java.util.Map;

public interface JsonReportService extends ReportFormatService {
    @Override
    byte[]generateReport(Map<String,Object>data, ReportingConstants.ReportType reportType,
                         ReportingConstants.OutputFormat format);

    @Override
    String getReportTitle(ReportingConstants.ReportType reportType);
}
