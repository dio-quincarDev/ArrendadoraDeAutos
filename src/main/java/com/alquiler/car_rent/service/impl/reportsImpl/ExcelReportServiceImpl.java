package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.service.reportService.ExcelReportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ExcelReportServiceImpl implements ExcelReportService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelReportServiceImpl.class);
    private static final String FONT_NAME = "Arial";
    private static final short HEADER_FONT_SIZE = 12;

    // 1. Métodos reutilizables para estilos (Nuevo)
    private XSSFCellStyle createHeaderStyle(XSSFWorkbook workbook) {
        XSSFFont font = workbook.createFont();
        font.setFontName(FONT_NAME);
        font.setBold(true);
        font.setFontHeightInPoints(HEADER_FONT_SIZE);
        font.setColor(IndexedColors.WHITE.getIndex());

        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 33, (byte) 82, (byte) 131}, null)); // Azul corporativo
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private XSSFCellStyle createDataStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    // 2. Método para crear celdas de forma segura (Nuevo)
    private void createSafeCell(Row row, int column, Object value, XSSFCellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof java.util.Date) {
            cell.setCellValue((java.util.Date) value);
        } else {
            cell.setCellValue(value != null ? value.toString() : "N/A");
        }
    }


        @Override
    public byte[] generateReport(Map<String, Object> data, ReportingConstants.ReportType reportType, ReportingConstants.OutputFormat format) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) { // 3. Try-with-resources (Mejorado)
            XSSFSheet sheet = workbook.createSheet(reportType.getTitle());

            // Usar arreglo para permitir modificación en lambdas
            final int[] rowNum = {0};

            // Estilos reutilizables
            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            XSSFCellStyle dataStyle = createDataStyle(workbook);

            switch (reportType) {
                case MOST_RENTED_VEHICLES:
                    Row headerRowMostRented = sheet.createRow(rowNum[0]++);
                    createSafeCell(headerRowMostRented, 0, "Vehículo", headerStyle);
                    createSafeCell(headerRowMostRented, 1, "Alquileres", headerStyle);

                    if (data.get("mostRentedVehicle") instanceof Map) {
                        Map<?, ?> mostRented = (Map<?, ?>) data.get("mostRentedVehicle");
                        Row dataRow = sheet.createRow(rowNum[0]++);
                        createSafeCell(dataRow, 0, mostRented.get("brand") + " " + mostRented.get("model"), dataStyle);
                        createSafeCell(dataRow, 1, mostRented.get("rentalCount"), dataStyle);
                    } else {
                        createSafeCell(sheet.createRow(rowNum[0]++), 0, "Sin datos disponibles", dataStyle);
                    }
                    break;

                case RENTAL_TRENDS:
                    Row headerRowTrends = sheet.createRow(rowNum[0]++);
                    createSafeCell(headerRowTrends, 0, "Período", headerStyle);
                    createSafeCell(headerRowTrends, 1, "Alquileres", headerStyle);

                    if (data.get("rentalTrends") instanceof List) {
                        List<Map<String, Object>> trends = (List<Map<String, Object>>) data.get("rentalTrends");
                        if (!trends.isEmpty()) {
                            trends.forEach(trend -> {
                                Row dataRow = sheet.createRow(rowNum[0]++);
                                createSafeCell(dataRow, 0, trend.get("period"), dataStyle);
                                createSafeCell(dataRow, 1, trend.get("rentalCount"), dataStyle);
                            });
                        } else {
                            createSafeCell(sheet.createRow(rowNum[0]++), 0, "Sin datos disponibles", dataStyle);
                        }
                    }
                    break;

                case RENTAL_SUMMARY:
                    createSafeCell(sheet.createRow(rowNum[0]++), 0, "RESUMEN DE ALQUILERES", headerStyle);

                    createSafeCell(sheet.createRow(rowNum[0]++), 0, "Total de Alquileres:", dataStyle);
                    createSafeCell(sheet.getRow(rowNum[0] - 1), 1, data.get("totalRentals"), dataStyle);

                    createSafeCell(sheet.createRow(rowNum[0]++), 0, "Clientes Únicos:", dataStyle);
                    createSafeCell(sheet.getRow(rowNum[0] - 1), 1, data.get("uniqueCustomers"), dataStyle);

                    createSafeCell(sheet.createRow(rowNum[0]++), 0, "Duración Promedio (días):", dataStyle);
                    createSafeCell(sheet.getRow(rowNum[0] - 1), 1, data.get("averageRentalDuration"), dataStyle);

                    if (data.get("mostRentedVehicle") instanceof Map) {
                        Map<?, ?> mostRented = (Map<?, ?>) data.get("mostRentedVehicle");
                        createSafeCell(sheet.createRow(rowNum[0]++), 0, "Vehículo Más Alquilado:", dataStyle);
                        createSafeCell(sheet.getRow(rowNum[0] - 1), 1,
                                mostRented.get("brand") + " " + mostRented.get("model") + " (" + mostRented.get("rentalCount") + ")",
                                dataStyle);
                    }

                    createSafeCell(sheet.createRow(rowNum[0]++), 0, "Ingresos Totales:", dataStyle);
                    createSafeCell(sheet.getRow(rowNum[0] - 1), 1, data.get("totalRevenue"), dataStyle);
                    break;

                case VEHICLE_USAGE:
                    Row headerRowVehicleUsage = sheet.createRow(rowNum[0]++);
                    createSafeCell(headerRowVehicleUsage, 0, "VEHÍCULO", headerStyle);
                    createSafeCell(headerRowVehicleUsage, 1, "USOS", headerStyle);

                    if (data.get("vehicleUsage") instanceof Map) {
                        Map<Vehicle, Long> usage = (Map<Vehicle, Long>) data.get("vehicleUsage");
                        usage.forEach((vehicle, count) -> {
                            Row row = sheet.createRow(rowNum[0]++);
                            createSafeCell(row, 0, vehicle.getBrand() + " " + vehicle.getModel(), dataStyle);
                            createSafeCell(row, 1, count, dataStyle);
                        });
                    } else {
                        createSafeCell(sheet.createRow(rowNum[0]++), 0, "Sin datos de uso", dataStyle);
                    }
                    break;

                case REVENUE_ANALYSIS:
                    createSafeCell(sheet.createRow(rowNum[0]++), 0, "ANÁLISIS DE INGRESOS", headerStyle);
                    createSafeCell(sheet.createRow(rowNum[0]++), 0, "Ingresos Totales:", dataStyle);
                    createSafeCell(sheet.getRow(rowNum[0] - 1), 1, data.get("totalRevenue"), dataStyle);

                    if (data.containsKey("totalRentals") && data.get("totalRentals") instanceof Number &&
                            data.containsKey("totalRevenue") && data.get("totalRevenue") instanceof Number) {
                        double avgRevenue = ((Number) data.get("totalRevenue")).doubleValue() / ((Number) data.get("totalRentals")).doubleValue();
                        createSafeCell(sheet.createRow(rowNum[0]++), 0, "Promedio por Alquiler:", dataStyle);
                        createSafeCell(sheet.getRow(rowNum[0] - 1), 1, avgRevenue, dataStyle);
                    }

                    if (data.get("rentalTrends") instanceof List) {
                        XSSFSheet trendsSheet = workbook.createSheet("Tendencias de Ingresos");
                        Row headerRowRevenueTrends = trendsSheet.createRow(0);
                        createSafeCell(headerRowRevenueTrends, 0, "Período", headerStyle);
                        createSafeCell(headerRowRevenueTrends, 1, "Alquileres", headerStyle);

                        List<Map<String, Object>> trends = (List<Map<String, Object>>) data.get("rentalTrends");
                        int trendsRowNum = 1;
                        for (Map<String, Object> trend : trends) {
                            Row row = trendsSheet.createRow(trendsRowNum++);
                            createSafeCell(row, 0, trend.get("period"), dataStyle);
                            createSafeCell(row, 1, trend.get("rentalCount"), dataStyle);
                        }
                        // Autoajuste para la hoja de tendencias
                        for (int i = 0; i < headerRowRevenueTrends.getLastCellNum(); i++) {
                            trendsSheet.autoSizeColumn(i);
                        }
                    }
                    break;

                case CUSTOMER_ACTIVITY:
                    createSafeCell(sheet.createRow(rowNum[0]++), 0, "ACTIVIDAD DE CLIENTES", headerStyle);
                    createSafeCell(sheet.createRow(rowNum[0]++), 0, "Clientes Únicos:", dataStyle);
                    createSafeCell(sheet.getRow(rowNum[0] - 1), 1, data.get("uniqueCustomers"), dataStyle);
                    createSafeCell(sheet.createRow(rowNum[0]++), 0, "Nuevos Clientes:", dataStyle);
                    createSafeCell(sheet.getRow(rowNum[0] - 1), 1, data.get("newCustomers"), dataStyle);

                    if (data.get("topCustomersByRentals") instanceof List) {
                        XSSFSheet topCustomersSheet = workbook.createSheet("Top Clientes por Alquileres");
                        Row headerRowTopCustomers = topCustomersSheet.createRow(0);
                        createSafeCell(headerRowTopCustomers, 0, "Cliente", headerStyle);
                        createSafeCell(headerRowTopCustomers, 1, "Total Alquileres", headerStyle);
                        createSafeCell(headerRowTopCustomers, 2, "Email", headerStyle); // Incluir email

                        List<Map<String, Object>> topCustomers = (List<Map<String, Object>>) data.get("topCustomersByRentals");
                        int topRowNum = 1;
                        for (Map<String, Object> customer : topCustomers) {
                            Row row = topCustomersSheet.createRow(topRowNum++);
                            createSafeCell(row, 0, customer.get("name"), dataStyle);
                            createSafeCell(row, 1, customer.get("rentalCount"), dataStyle);
                            createSafeCell(row, 2, customer.get("email"), dataStyle); // Incluir email
                        }
                        // Autoajuste para la hoja de top clientes
                        for (int i = 0; i < headerRowTopCustomers.getLastCellNum(); i++) {
                            topCustomersSheet.autoSizeColumn(i);
                        }
                    }
                    break;

                default:
                    Row defaultRow = sheet.createRow(rowNum[0]++);
                    createSafeCell(defaultRow, 0, "Reporte no implementado: " + reportType, dataStyle);
                    break;
            }

            // Autoajuste de columnas optimizado
            if (sheet.getRow(0) != null) {
                for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            return writeToBytes(workbook);

        } catch (Exception e) {
            logger.error("Error crítico generando reporte: {}", e.getMessage(), e);
            throw new RuntimeException("Error generando reporte: " + e.getMessage(), e); // 4. Mejor manejo de errores
        }
    }

    // 5. Método centralizado para escribir el archivo (Nuevo)
    private byte[] writeToBytes(XSSFWorkbook workbook) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // Mantener métodos originales con ajustes mínimos
    @Override
    public byte[] generateGenericTableExcel(List<String> headers, List<List<String>> data) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Reporte");

            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            XSSFCellStyle dataStyle = createDataStyle(workbook);

            // Cabeceras
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                createSafeCell(headerRow, i, headers.get(i), headerStyle);
            }

            // Datos
            if (data != null && !data.isEmpty()) {
                for (int i = 0; i < data.size(); i++) {
                    Row row = sheet.createRow(i + 1);
                    List<String> rowData = data.get(i);
                    for (int j = 0; j < rowData.size(); j++) {
                        createSafeCell(row, j, rowData.get(j), dataStyle);
                    }
                }
            }

            // Autoajuste
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            return writeToBytes(workbook);
        } catch (IOException e) {
            logger.error("Error generando tabla genérica: {}", e.getMessage(), e);
            throw new RuntimeException("Error en tabla genérica: " + e.getMessage(), e);
        }
    }

    @Override
    public String getReportTitle(ReportingConstants.ReportType reportType) {
        return reportType.getTitle() + " - " + java.time.LocalDate.now();
    }
}