package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Vehicle;
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
public class ExcelReportServiceImpl implements ExcelReportService {

    private final static Logger logger = LoggerFactory.getLogger(ExcelReportServiceImpl.class);

    @Override
    public byte[] generateGenericTableExcel(List<String> headers, List<List<String>> data) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte");

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

        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            logger.error("Error al generar el archivo Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el reporte Excel", e);
        } finally {
            try {
                outputStream.close();
                workbook.close();
            } catch (IOException e) {
                logger.error("Error al cerrar recursos de Excel: {}", e.getMessage(), e);
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
                case MOST_RENTED_VEHICLES:
                    Map<Vehicle, Long> rentalCounts = (Map<Vehicle, Long>) data.get("rentalCountsByVehicle");
                    if (rentalCounts != null && !rentalCounts.isEmpty()) {
                        row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue("Vehículo");
                        row.createCell(1).setCellValue("Cantidad de Alquileres");
                        for (Map.Entry<Vehicle, Long> entry : rentalCounts.entrySet()) {
                            row = sheet.createRow(rowNum++);
                            row.createCell(0).setCellValue(entry.getKey().getBrand() + " " + entry.getKey().getModel());
                            row.createCell(1).setCellValue(entry.getValue());
                        }
                    } else {
                        row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue("No hay datos disponibles para los vehículos más alquilados.");
                    }
                    break;
                case RENTAL_TRENDS:
                    List<Map<String, Object>> rentalTrends = (List<Map<String, Object>>) data.get("rentalTrends");
                    if (rentalTrends != null && !rentalTrends.isEmpty()) {
                        row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue("Período");
                        row.createCell(1).setCellValue("Cantidad de Alquileres");
                        for (Map<String, Object> trend : rentalTrends) {
                            row = sheet.createRow(rowNum++);
                            row.createCell(0).setCellValue(String.valueOf(trend.get("period")));
                            row.createCell(1).setCellValue(String.valueOf(trend.get("rentalCount")));
                        }
                    } else {
                        row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue("No hay datos disponibles para las tendencias de alquileres.");
                    }
                    break;
                case VEHICLE_USAGE:
                    Map<Vehicle, Long> usage = (Map<Vehicle, Long>) data.get("vehicleUsage");
                    if (usage != null && !usage.isEmpty()) {
                        row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue("Vehículo");
                        row.createCell(1).setCellValue("Cantidad de Usos");
                        for (Map.Entry<Vehicle, Long> entry : usage.entrySet()) {
                            row = sheet.createRow(rowNum++);
                            row.createCell(0).setCellValue(entry.getKey().getBrand() + " " + entry.getKey().getModel());
                            row.createCell(1).setCellValue(entry.getValue());
                        }
                    } else {
                        row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue("No hay datos disponibles para el uso de vehículos.");
                    }
                    break;
                case RENTAL_SUMMARY:
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Resumen de Alquileres");
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Total de Alquileres:");
                    row.createCell(1).setCellValue(String.valueOf(data.get("totalRentals")));
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Clientes Únicos:");
                    row.createCell(1).setCellValue(String.valueOf(data.get("uniqueCustomers")));
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Duración Promedio (días):");
                    row.createCell(1).setCellValue(String.format("%.2f", data.get("averageRentalDuration")));
                    Map<String, Object> mostRented = (Map<String, Object>) data.get("mostRentedVehicle");
                    if (mostRented != null) {
                        row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue("Vehículo Más Alquilado:");
                        row.createCell(1).setCellValue(mostRented.get("brand") + " " + mostRented.get("model") + " (" + mostRented.get("rentalCount") + " veces)");
                    }
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Ingresos Totales:");
                    row.createCell(1).setCellValue(String.format("%.2f", data.get("totalRevenue")));
                    break;
                case REVENUE_ANALYSIS:
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Análisis de Ingresos");
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Ingresos Totales:");
                    row.createCell(1).setCellValue(String.format("%.2f", data.get("totalRevenue")));
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Ingresos Promedio por Alquiler:");
                    row.createCell(1).setCellValue(String.format("%.2f", (Double) data.get("totalRevenue") / (Long) data.get("totalRentals")));
                    List<Map<String, Object>> revenueTrends = (List<Map<String, Object>>) data.get("rentalTrends");
                    if (revenueTrends != null && !revenueTrends.isEmpty()) {
                        row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue("Tendencias de Ingresos (Estimado):");
                        Sheet trendsSheet = workbook.createSheet("Tendencias de Ingresos");
                        int trendsRowNum = 0;
                        Row headerRowTrends = trendsSheet.createRow(trendsRowNum++);
                        headerRowTrends.createCell(0).setCellValue("Período");
                        headerRowTrends.createCell(1).setCellValue("Ingresos Estimados");
                        double averageRate = (Double) data.getOrDefault("averageRentalRate", 50.0);
                        for (Map<String, Object> trend : revenueTrends) {
                            Row dataRowTrends = trendsSheet.createRow(trendsRowNum++);
                            dataRowTrends.createCell(0).setCellValue(String.valueOf(trend.get("period")));
                            dataRowTrends.createCell(1).setCellValue(String.format("%.2f", (Long) trend.get("rentalCount") * averageRate));
                        }
                    }
                    break;
                case CUSTOMER_ACTIVITY:
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Actividad de Clientes");
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Total de Clientes Activos:");
                    row.createCell(1).setCellValue(String.valueOf(data.get("activeCustomers")));
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Nuevos Clientes:");
                    row.createCell(1).setCellValue(String.valueOf(data.get("newCustomers")));
                    List<Map<String, Object>> topCustomers = (List<Map<String, Object>>) data.get("topCustomersByRentals");
                    if (topCustomers != null && !topCustomers.isEmpty()) {
                        row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue("Clientes con Mayor Actividad:");
                        Sheet topCustomersSheet = workbook.createSheet("Top Clientes");
                        int topCustomersRowNum = 0;
                        Row headerRowTopCustomers = topCustomersSheet.createRow(topCustomersRowNum++);
                        headerRowTopCustomers.createCell(0).setCellValue("ID Cliente");
                        headerRowTopCustomers.createCell(1).setCellValue("Nombre Cliente");
                        headerRowTopCustomers.createCell(2).setCellValue("Cantidad de Alquileres");
                        for (Map<String, Object> customer : topCustomers) {
                            Row dataRowTopCustomers = topCustomersSheet.createRow(topCustomersRowNum++);
                            dataRowTopCustomers.createCell(0).setCellValue(String.valueOf(customer.get("customerId")));
                            dataRowTopCustomers.createCell(1).setCellValue(String.valueOf(customer.get("name")));
                            dataRowTopCustomers.createCell(2).setCellValue(String.valueOf(customer.get("rentalCount")));
                        }
                        Map<String, Double> avgDurationByCustomer = (Map<String, Double>) data.get("averageRentalDurationByTopCustomers");
                        if (avgDurationByCustomer != null && !avgDurationByCustomer.isEmpty()) {
                            Sheet avgDurationSheet = workbook.createSheet("Promedio Duración por Cliente");
                            int avgDurationRowNum = 0;
                            Row headerRowAvgDuration = avgDurationSheet.createRow(avgDurationRowNum++);
                            headerRowAvgDuration.createCell(0).setCellValue("Nombre Cliente");
                            headerRowAvgDuration.createCell(1).setCellValue("Duración Promedio (días)");
                            for (Map.Entry<String, Double> entry : avgDurationByCustomer.entrySet()) {
                                Row dataRowAvgDuration = avgDurationSheet.createRow(avgDurationRowNum++);
                                dataRowAvgDuration.createCell(0).setCellValue(entry.getKey());
                                dataRowAvgDuration.createCell(1).setCellValue(String.format("%.2f", entry.getValue()));
                            }
                        }
                        break;
                    }
                    break;
                case ReportingConstants.ReportType.GENERIC_METRICS:
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Métrica");
                    row.createCell(1).setCellValue("Valor");

                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Total de Alquileres");
                    row.createCell(1).setCellValue(String.valueOf(data.get("totalRentals")));

                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Ingresos Totales");
                    row.createCell(1).setCellValue(String.format("%.2f", data.get("totalRevenue")));

                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Vehículos Únicos Alquilados");
                    row.createCell(1).setCellValue(String.valueOf(data.get("uniqueVehicles")));

                    if (data.get("mostRentedVehicle") != null) {
                        Map<String, Object> mostRentedVehicle = (Map<String, Object>) data.get("mostRentedVehicle");
                        row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue("Vehículo Más Alquilado");
                        row.createCell(1).setCellValue(mostRentedVehicle.get("brand") + " " + mostRentedVehicle.get("model") + " (" + mostRentedVehicle.get("rentalCount") + " veces)");
                    }

                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Nuevos Clientes");
                    row.createCell(1).setCellValue(String.valueOf(data.get("newCustomers")));
                    break;
                default:
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("Reporte de Excel no implementado para: " + reportType.getTitle());
                    break;
            }

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