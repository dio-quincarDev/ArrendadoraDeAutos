package com.alquiler.car_rent.service.impl;

import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.repositories.CustomerRepository;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.repositories.VehicleRepository;
import com.alquiler.car_rent.service.ReportingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.w3c.dom.DOMImplementation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportingServiceImpl implements ReportingService {

    private static final Logger logger = LoggerFactory.getLogger(ReportingServiceImpl.class);
    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;

    public ReportingServiceImpl(
            RentalRepository rentalRepository,
            VehicleRepository vehicleRepository,
            CustomerRepository customerRepository,
            ObjectMapper objectMapper
    ) {
        this.rentalRepository = rentalRepository;
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> generateReportData(TimePeriod period, LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate;
        LocalDate end = endDate;

        if (start == null) {
            end= LocalDate.now();
            start = endDate.minus(period.getValue(), period.getUnit());
        }
        
        // Ahora (usando search con paginación):
        int page = 0;
        int size = 500;
        Pageable pageable = PageRequest.of(page, size);
        Page<Rental> rentalsPage = rentalRepository.search(
          toDateTime(start),
            toDateTime(end != null ? LocalDate.from(end.plusDays(1).atStartOfDay()) : null),
            Pageable.unpaged() // O usar paginación: PageRequest.of(0, 1000)
        );
        List<Rental> rentals = rentalsPage.getContent();


        Map<String, Object> reportData = new HashMap<>();
        reportData.put("startDate", start);
        reportData.put("endDate", end);
        reportData.put("period", period);
        reportData.put("totalRentals", (long) rentals.size());
        reportData.put("totalPages", rentalsPage.getTotalPages());
        reportData.put("currentPage", rentalsPage.getNumber());

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
        } catch (DocumentException e) {
            logger.error("Error al generar reporte PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar reporte PDF", e);
        } catch (IOException e) {
            logger.error("Error de I/O al generar reporte (puede ser PDF, JSON, Excel o Chart): {}", e.getMessage(), e);
            throw new RuntimeException("Error de I/O al generar reporte", e);
        } catch (IllegalArgumentException e) {
            logger.error("Formato de reporte no soportado: {}", e.getMessage());
            throw e; // Re-lanzamos la excepción para que el controlador pueda manejarla
        } catch (Exception e) {
            logger.error("Error inesperado al generar reporte: {}", e.getMessage(), e);
            throw new RuntimeException("Error inesperado al generar reporte", e);
        }
    }

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
                rootNode.put("totalRentals", data.get("totalRentals").toString());
                rootNode.put("totalRevenue", data.get("totalRevenue").toString());
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
                LocalDate startDate = (LocalDate) data.get("startDate");
                LocalDate endDate = (LocalDate) data.get("endDate");

                Map<String, Double> monthlyRevenue = rentalRepository.findInDateRange(
                        toDateTime(startDate),
                        toDateTime(endDate.plusDays(1))
                ).stream()
                        .collect(Collectors.groupingBy(
                                rental-> rental.getStartDate().format(DateTimeFormatter.ofPattern("yyy-MM")),
                                Collectors.summingDouble(rental-> rental.getTotalPrice().doubleValue())
                        ));
                monthlyRevenue.forEach(pieDataset::setValue);
                
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
        if (chart != null) {
            if (svg) {
                DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
                String svgNS = "http://www.w3.org/2000/svg";
                Document document = (Document) domImpl.createDocument(svgNS, "svg", null);
                SVGGraphics2D svgGraphics2D = new SVGGraphics2D((org.w3c.dom.Document) document);
                chart.draw(svgGraphics2D, new java.awt.Rectangle(800, 600));
                try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8")) {
                    svgGraphics2D.stream(writer, true);
                } finally {
                    svgGraphics2D.dispose();
                }
            } else {
                ChartUtils.writeChartAsPNG(outputStream, chart, 800, 600);
            }
            return outputStream.toByteArray();
        }

        return new byte[0]; // Devolver un array vacío si no se generó el gráfico
    }

    private byte[] generateExcelReport(ReportType reportType, Map<String, Object> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet summarySheet = workbook.createSheet("Resumen de Ingresos");
        Sheet detailsSheet = workbook.createSheet("Detalles de Alquileres");

        // Estilos para los encabezados
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);

        LocalDate startDate = (LocalDate) data.get("startDate");
        LocalDate endDate = (LocalDate) data.get("endDate");
        Long totalRentals = (Long) data.get("totalRentals");
        Double totalRevenue = (Double) data.get("totalRevenue");
        
        List<Rental> rentals = rentalRepository.findInDateRange(
                toDateTime(startDate),
                toDateTime(endDate.plusDays(1))
        );

        // --- Hoja de Resumen de Ingresos ---
        Row titleRow = summarySheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Resumen de Ingresos por Alquiler");
        org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
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
                row.createCell(2).setCellValue(rental.getCustomer().getName());
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
    
    // Simplified date conversion methods
    private LocalDateTime toDateTime(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    @Override
    public long getTotalRentals(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate;
        LocalDate end = endDate;
        if(start==null){
            end = LocalDate.now();
            start = end.minus(TimePeriod.MONTHLY.getValue(), TimePeriod.MONTHLY.getUnit());
        }

        Page<Rental> rentalsPage = rentalRepository.search(
                toDateTime(start),
                toDateTime(end != null ? LocalDate.from(end.plusDays(1).atStartOfDay()) : null),
                Pageable.unpaged()
        );
        
        return rentalsPage.getTotalElements();
    }

    @Override
    public double getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate;
        LocalDate end = endDate;
        if(start==null){
            end = LocalDate.now();
            start = end.minus(TimePeriod.MONTHLY.getValue(), TimePeriod.MONTHLY.getUnit());
        }
        Page<Rental> rentalsPage = rentalRepository.search(
                toDateTime(start),
                toDateTime(end != null ? LocalDate.from(end.plusDays(1).atStartOfDay()) : null),
                Pageable.unpaged()
        );
        return rentalsPage.getContent().stream()
                .mapToDouble(rental-> rental.getTotalPrice().doubleValue())
                .sum();
    }

    @Override
    public long getUniqueVehiclesRented(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate;
        LocalDate end = endDate;
        if(start==null){
            end = LocalDate.now();
            start = end.minus(TimePeriod.MONTHLY.getValue(), TimePeriod.MONTHLY.getUnit());
        }

        Page<Rental> rentalsPage = rentalRepository.search(
                toDateTime(start),
                toDateTime(end != null ? LocalDate.from(end.plusDays(1).atStartOfDay()) : null),
                Pageable.unpaged()
        );
        return rentalsPage.getContent().stream()
                .map(Rental::getVehicle)
                .distinct()
                .count();
    }

    @Override
    public Map<String, Object> getMostRentedVehicle(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate;
        LocalDate end = endDate;
        if(start==null){
            end = LocalDate.now();
            start = end.minus(TimePeriod.MONTHLY.getValue(), TimePeriod.MONTHLY.getUnit());
        }

        Page<Rental> rentalsPage = rentalRepository.search(
                toDateTime(start),
                toDateTime(end != null ? LocalDate.from(end.plusDays(1).atStartOfDay()) : null),
                Pageable.unpaged()
        );
        
        return rentalsPage.getContent().stream()
        .collect(Collectors.groupingBy(Rental::getVehicle, Collectors.counting()))
        .entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(entry -> {
            Map<String, Object> result = new HashMap<>(); // Explicitly define the types
            result.put("brand", entry.getKey().getBrand());
            result.put("model", entry.getKey().getModel());
            result.put("rentalCount", entry.getValue());
            return result;
        })
        .orElse(new HashMap<>());
    }

    @Override
    public long getNewCustomersCount(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate;
        LocalDate end = endDate;
        if (start == null) {
            end = LocalDate.now();
            start = end.minus(TimePeriod.MONTHLY.getValue(), TimePeriod.MONTHLY.getUnit());
        }
        // Cambio: Asegurando que los valores null se manejen en la consulta o aquí
        return customerRepository.countByCreatedAtBetween(
                toDateTime(start),
                toDateTime(end != null ? LocalDate.from(end.plusDays(1).atStartOfDay()) : null)
        );
    }

    @Override
    public List<Map<String, Object>> getRentalTrends(TimePeriod period, LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate;
        LocalDate end = endDate;
        if (start == null) {
            end = LocalDate.now();
            start = end.minus(period.getValue(), period.getUnit());
        }

        DateTimeFormatter formatter;
        
        switch (period) {
            case MONTHLY:
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                break;
            case QUARTERLY:
                formatter = DateTimeFormatter.ofPattern("yyyy-QQQ");
                break;
            case BIANNUAL:
                formatter = DateTimeFormatter.ofPattern("yyyy-'H'");
                break;
            case ANNUAL:
                formatter = DateTimeFormatter.ofPattern("yyyy");
                break;
            default:
                formatter = DateTimeFormatter.ISO_DATE;
                break;
        }

        LocalDate current = start;
        List<Map<String, Object>> trends = new ArrayList<>();

        while (!current.isAfter(end)) {
            LocalDate next = current.plus(period.getValue(), period.getUnit());
            Page<Rental> rentalsPage = rentalRepository.search(
                    toDateTime(current),
                    toDateTime(next),
                    Pageable.unpaged()
            );
            trends.add(Map.of("period", current.format(formatter), "rentalCount", rentalsPage.getTotalElements()));
            current = next;
        }

        return trends;
    }

	
}