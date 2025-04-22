package com.alquiler.car_rent.service.reportService;

import com.alquiler.car_rent.commons.constants.ReportingConstants;

import java.util.Map;

public interface ReportFormatService {
    /**
     * Genera un reporte en el formato específico
     */
    byte[] generateReport(Map<String, Object> data, ReportingConstants.ReportType reportType,
                          ReportingConstants.OutputFormat format);

    /**
     * Obtiene el título del reporte según su tipo
     */
    String getReportTitle(ReportingConstants.ReportType reportType);
}
