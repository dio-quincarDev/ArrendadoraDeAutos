package com.alquiler.car_rent.service.impl;

import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.repositories.VehicleRepository;
import com.alquiler.car_rent.service.ReportingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Font;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.List;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportingServiceImpl implements ReportingService {

    private static final Logger logger = LoggerFactory.getLogger(ReportingServiceImpl.class);
    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;
    private final ObjectMapper objectMapper;

    public ReportingServiceImpl(
        RentalRepository rentalRepository,
        VehicleRepository vehicleRepository, 
        ObjectMapper objectMapper
    ) {
        this.rentalRepository = rentalRepository;
        this.vehicleRepository = vehicleRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> generateReportData(TimePeriod period, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minus(period.getValue(), period.getUnit());
        }

        List<Rental> rentals = rentalRepository.findByEndDateBetween(
            startDate.atStartOfDay(),
            endDate.plusDays(1).atStartOfDay()
        );

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("startDate", startDate);
        reportData.put("endDate", endDate);
        reportData.put("period", period);
        reportData.put("totalRentals", (long) rentals.size());

        double totalRevenue = rentals.stream()
            .mapToDouble(rental -> rental.getTotalPrice().doubleValue())
            .sum();
        reportData.put("totalRevenue", totalRevenue);

        Map<Vehicle, Long> rentalCountsByVehicle = rentals.stream()
            .collect(Collectors.groupingBy(Rental::getVehicle, Collectors.counting()));
        reportData.put("rentalCountsByVehicle", rentalCountsByVehicle);

        return reportData;
    }

    @Override
    public byte[] generateReport(
        OutputFormat format, 
        ReportType reportType, 
        TimePeriod period, 
        LocalDate startDate, 
        LocalDate endDate
    ) {
        Map<String, Object> reportData = generateReportData(period, startDate, endDate);
        try {
            switch (format) {
                case PDF: return generatePdfReport(reportType, reportData);
                case JSON: return generateJsonReport(reportType, reportData);
                case CHART_PNG: return generateChartReport(reportType, reportData, false);
                case CHART_SVG: return generateChartReport(reportType, reportData, true);
                case EXCEL: return generateExcelReport(reportType, reportData);
                default: throw new IllegalArgumentException("Formato no soportado: " + format);
            }
        } catch (Exception e) {
            logger.error("Error generando reporte: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar reporte", e);
        }
    }

    // Métodos auxiliares (generatePdfReport, generateJsonReport, etc.) como antes...
    private byte[] generatePdfReport(ReportType reportType, Map<String, Object> data) throws DocumentException, IOException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            
            // Cabecera estándar
            addPdfHeader(document, reportType, data);
            
            // Contenido dinámico
            switch (reportType) {
                case RENTAL_SUMMARY:
                    addRentalSummaryContent(document, data);
                    break;
                case MOST_RENTED_CARS:
                    addMostRentedCarsContent(document, data);
                    break;
                default:
                    document.add(new Paragraph("Tipo de reporte no soportado"));
            }
            
            return out.toByteArray();
        } finally {
            document.close();
        }
    }
    

    private void addPdfHeader(Document doc, ReportType type, Map<String, Object> data) throws DocumentException {
        doc.add(new Paragraph("Reporte de " + getReportTitle(type), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        doc.add(new Paragraph("Período: " + formatDate((LocalDate)data.get("startDate")) + " a " + 
                             formatDate((LocalDate)data.get("endDate"))));
        doc.add(Chunk.NEWLINE);
    }

    private void addRentalSummaryContent(Document doc, Map<String, Object> data) throws DocumentException {
        doc.add(new Paragraph("Total de alquileres: " + data.get("totalRentals")));
        doc.add(new Paragraph("Ingresos totales: $" + String.format("%,.2f", data.get("totalRevenue"))));
    }

    private void addMostRentedCarsContent(Document doc, Map<String, Object> data) throws DocumentException {
        Paragraph title = new Paragraph("Vehículos más alquilados:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
        doc.add(title);
        
        @SuppressWarnings("unchecked")
        Map<Vehicle, Long> rentals = (Map<Vehicle, Long>) data.get("rentalCountsByVehicle");
        
        rentals.entrySet().stream()
            .sorted(Map.Entry.<Vehicle, Long>comparingByValue().reversed())
            .limit(10)
            .forEach(entry -> {
                try {
                    doc.add(new Paragraph("- " + entry.getKey().getBrand() + " " + entry.getKey().getModel() + 
                                       ": " + entry.getValue() + " alquileres"));
                } catch (DocumentException e) {
                    throw new RuntimeException(e);
                }
            });
    }
    
    private byte[] generateJsonReport(ReportType reportType, Map<String, Object> data) throws IOException {
        ObjectNode rootNode = objectMapper.createObjectNode();
        
        // Agregar metadatos
        rootNode.put("reportType", reportType.name());
        rootNode.put("startDate", formatDate((LocalDate)data.get("startDate")));
        rootNode.put("endDate", formatDate((LocalDate)data.get("endDate")));
        rootNode.put("period", ((TimePeriod)data.get("period")).name());
        
        // Agregar datos específicos según el tipo de reporte
        switch (reportType) {
            case RENTAL_SUMMARY:
                rootNode.put("totalRentals", (Integer)data.get("totalRentals"));
                rootNode.put("totalRevenue", (Double)data.get("totalRevenue"));
                break;
            case MOST_RENTED_CARS:
                ArrayNode vehiclesArray = rootNode.putArray("mostRentedVehicles");
                @SuppressWarnings("unchecked")
                Map<Vehicle, Long> rentalCounts = (Map<Vehicle, Long>) data.get("rentalCountsByVehicle");
                
                rentalCounts.entrySet().stream()
                    .sorted(Map.Entry.<Vehicle, Long>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        ObjectNode vehicleNode = vehiclesArray.addObject();
                        vehicleNode.put("brand", entry.getKey().getBrand());
                        vehicleNode.put("model", entry.getKey().getModel());
                        vehicleNode.put("rentalCount", entry.getValue());
                    });
                break;
            // Implementar otros tipos de reportes
        }
        
        return objectMapper.writeValueAsBytes(rootNode);
    }
    
    private byte[] generateChartReport(ReportType reportType, Map<String, Object> data, boolean svg) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JFreeChart chart;
        
        switch (reportType) {
            case MOST_RENTED_CARS:
                // Crear dataset para el gráfico
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                @SuppressWarnings("unchecked")
                Map<Vehicle, Long> rentalCounts = (Map<Vehicle, Long>) data.get("rentalCountsByVehicle");
                
                rentalCounts.entrySet().stream()
                    .sorted(Map.Entry.<Vehicle, Long>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        String vehicle = entry.getKey().getBrand() + " " + entry.getKey().getModel();
                        dataset.addValue(entry.getValue(), "Alquileres", vehicle);
                    });
                
                // Crear gráfico de barras
                chart = ChartFactory.createBarChart(
                    "Vehículos más alquilados", // título
                    "Vehículo",                 // eje x
                    "Cantidad de alquileres",   // eje y
                    dataset
                );
                break;
                
            case REVENUE_ANALYSIS:
                // Crear un dataset para gráfico de pastel para ingresos por período
                DefaultPieDataset pieDataset = new DefaultPieDataset();
                // Aquí se agregarían datos reales de ingresos por período
                pieDataset.setValue("Enero", 25000);
                pieDataset.setValue("Febrero", 30000);
                pieDataset.setValue("Marzo", 28000);
                
                chart = ChartFactory.createPieChart(
                    "Distribución de Ingresos",
                    pieDataset,
                    true, // incluir leyenda
                    true, // tooltips
                    false // URLs
                );
                break;
                
            default:
                throw new IllegalArgumentException("Tipo de gráfico no implementado: " + reportType);
        }
        
        // Renderizar como PNG
        if (svg) {
            // Implementar exportación a SVG (requiere biblioteca adicional)
            throw new UnsupportedOperationException("Exportación a SVG no implementada");
        } else {
            ChartUtils.writeChartAsPNG(outputStream, chart, 800, 600);
        }
        
        return outputStream.toByteArray();
    }


    private byte[] generateExcelReport(ReportType reportType, Map<String, Object> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet summarySheet = workbook.createSheet("Resumen de Ingresos");
        Sheet detailsSheet = workbook.createSheet("Detalles de Alquileres");

        // Estilos para los encabezados
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);

        LocalDate startDate = (LocalDate) data.get("startDate");
        LocalDate endDate = (LocalDate) data.get("endDate");
        Long totalRentals = (Long) data.get("totalRentals");
        Double totalRevenue = (Double) data.get("totalRevenue");
        @SuppressWarnings("unchecked")
        List<Rental> rentals = rentalRepository.findByEndDateBetween(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
        );

        // --- Hoja de Resumen de Ingresos ---
        Row titleRow = summarySheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Resumen de Ingresos por Alquiler");
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);

        Row dateStartRow = summarySheet.createRow(2);
        dateStartRow.createCell(0).setCellValue("Fecha de Inicio");
        dateStartRow.createCell(1).setCellValue(formatDate(startDate));

        Row dateEndRow = summarySheet.createRow(3);
        dateEndRow.createCell(0).setCellValue("Fecha de Fin");
        dateEndRow.createCell(1).setCellValue(formatDate(endDate));

        Row totalRevenueRow = summarySheet.createRow(5);
        totalRevenueRow.createCell(0).setCellValue("Total de Ingresos Recaudados");
        Cell revenueCell = totalRevenueRow.createCell(1);
        revenueCell.setCellValue(totalRevenue);
        CellStyle currencyStyle = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();
        currencyStyle.setDataFormat(dataFormat.getFormat("$#,##0.00"));
        revenueCell.setCellStyle(currencyStyle);

        Row totalRentalsRow = summarySheet.createRow(6);
        totalRentalsRow.createCell(0).setCellValue("Total de Alquileres");
        totalRentalsRow.createCell(1).setCellValue(totalRentals);

        // --- Hoja de Detalles de Alquileres ---
        Row headerRowDetails = detailsSheet.createRow(0);
        String[] headers = {"ID Alquiler", "ID Cliente", "Nombre Cliente", "ID Vehículo", "Marca Vehículo", "Modelo Vehículo", "Fecha Inicio Alquiler", "Fecha Fin Alquiler", "Precio Total"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRowDetails.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Rental rental : rentals) {
            Row row = detailsSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rental.getId());

            // Acceder a la ID del cliente a través de la relación
            if (rental.getCustomer() != null) {
                row.createCell(1).setCellValue(rental.getCustomer().getId());
                row.createCell(2).setCellValue(rental.getCustomer().getName()); // Asumiendo que la entidad Customer tiene un campo 'name'
            } else {
                row.createCell(1).setCellValue("");
                row.createCell(2).setCellValue("");
            }

            // Acceder a la información del vehículo a través de la relación
            if (rental.getVehicle() != null) {
                row.createCell(3).setCellValue(rental.getVehicle().getId());
                row.createCell(4).setCellValue(rental.getVehicle().getBrand());
                row.createCell(5).setCellValue(rental.getVehicle().getModel());
            } else {
                row.createCell(3).setCellValue("");
                row.createCell(4).setCellValue("");
                row.createCell(5).setCellValue("");
            }

            row.createCell(6).setCellValue(formatDate(LocalDate.from(rental.getStartDate())));
            row.createCell(7).setCellValue(formatDate(LocalDate.from(rental.getEndDate())));
            Cell priceCell = row.createCell(8);
            priceCell.setCellValue(rental.getTotalPrice().doubleValue());
            priceCell.setCellStyle(currencyStyle);
        }

        // Ajustar el ancho de las columnas para que quepa el contenido
        for (int i = 0; i < headers.length; i++) {
            detailsSheet.autoSizeColumn(i);
        }
        summarySheet.autoSizeColumn(0);
        summarySheet.autoSizeColumn(1);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }
    
    private String getReportTitle(ReportType reportType) {
        switch (reportType) {
            case RENTAL_SUMMARY: return "Resumen de Alquileres";
            case VEHICLE_USAGE: return "Uso de Vehículos";
            case REVENUE_ANALYSIS: return "Análisis de Ingresos";
            case CUSTOMER_ACTIVITY: return "Actividad de Clientes";
            case MOST_RENTED_CARS: return "Vehículos Más Alquilados";
            default: return "Reporte";
        }
    }
    
    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    
    
    }
}