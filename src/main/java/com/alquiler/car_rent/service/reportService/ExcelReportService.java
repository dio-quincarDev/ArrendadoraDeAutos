package com.alquiler.car_rent.service.reportService;


import java.util.List;

// ✅ Final recomendado
public interface ExcelReportService extends ReportFormatService {
    byte[] generateGenericTableExcel(List<String> headers, List<List<String>> data);
}
