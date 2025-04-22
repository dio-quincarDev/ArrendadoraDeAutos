package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.service.reportService.ExcelReportGenericTable;
import com.alquiler.car_rent.service.reportService.ExcelReportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ExcelReportGenericServiceImpl implements ExcelReportGenericTable {

    private final static Logger logger = LoggerFactory.getLogger(ExcelReportGenericServiceImpl.class);

    @Override
    public byte[] generateGenericTableExcel(List<String> headers, List<List<String>> data) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Datos"); // Nombre de la hoja para datos genéricos

        // Crear la fila de encabezados
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

        // Crear las filas de datos
        CellStyle dataStyle = workbook.createCellStyle();
        Font dataFont = workbook.createFont();
        dataStyle.setFont(dataFont);

        for (int i = 0; i < data.size(); i++) {
            Row dataRow = sheet.createRow(i + 1);
            List<String> rowData = data.get(i);
            for (int j = 0; j < rowData.size(); j++) {
                Cell cell = dataRow.createCell(j);
                cell.setCellValue(rowData.get(j));
                cell.setCellStyle(dataStyle);
            }
        }

        // Ajustar el ancho de las columnas al contenido
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            logger.error("Error al generar el archivo Excel genérico: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el reporte Excel genérico", e);
        } finally {
            try {
                outputStream.close();
                workbook.close();
            } catch (IOException e) {
                logger.error("Error al cerrar recursos de Excel genérico: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public byte[] generateReport(Map<String, Object> data, ReportingConstants.ReportType reportType, ReportingConstants.OutputFormat format) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(reportType.getTitle());
        int rowNum = 0;
        Row row;
        Cell cell;

        try {
            switch (reportType) {
                // ... implementación existente para otros ReportType ...
                default:
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Reporte de Excel no implementado para: " + reportType.getTitle());
                    break;
            }

            // Ajustar el ancho de las columnas al contenido de la hoja principal
            if (sheet.getPhysicalNumberOfRows() > 0) {
                int lastCellNum = sheet.getRow(0).getLastCellNum();
                for (int i = 0; i < lastCellNum; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            logger.error("Error al generar el reporte de Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el reporte de Excel", e);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                logger.error("Error al cerrar el libro de Excel: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public String getReportTitle(ReportingConstants.ReportType reportType) {
        return reportType.getTitle() + " (Excel)";
    }
}