package com.alquiler.car_rent.service.reportService;

import java.util.List;

public interface ExcelReportService extends ReportFormatService {
    /**
     * Genera un archivo Excel genérico a partir de una tabla de datos
     */
    byte[] generateGenericTableExcel(List<String> headers, List<List<String>> data);
}
