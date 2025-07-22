package com.alquiler.car_rent.service.reportService;

import com.alquiler.car_rent.commons.constants.ReportingConstants;

import java.util.Map;

public interface ChartReportService extends ReportFormatService {
    /**
     * Genera un gráfico en formato PNG
     */
    byte[] generateChartAsPng(Map<String, Object> data, ReportingConstants.ReportType reportType);

    /**
     * Genera un gráfico en formato SVG
     */
    byte[] generateChartAsSvg(Map<String, Object> data, ReportingConstants.ReportType reportType);
}
