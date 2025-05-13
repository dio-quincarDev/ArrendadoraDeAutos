package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.exceptions.GlobalExceptionHandler;
import com.alquiler.car_rent.service.reportService.PdfReportService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class PdfReportServiceImpl implements PdfReportService {

    private static final Logger logger = LoggerFactory.getLogger(PdfReportServiceImpl.class);
    private static final Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLDITALIC);
    private static final Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font boldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private static final Font italicFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
    private static final Font boldItalicFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLDITALIC);
    private static final Font normalFont = new Font(Font.FontFamily.HELVETICA, 10);
    private static final BaseColor headerBackgroundColor = BaseColor.LIGHT_GRAY;

    private final GlobalExceptionHandler globalExceptionHandler;

    public PdfReportServiceImpl(GlobalExceptionHandler globalExceptionHandler) {
        this.globalExceptionHandler = globalExceptionHandler;
    }

    @Override
    public byte[] generateReport(Map<String, Object> data, ReportingConstants.ReportType reportType, ReportingConstants.OutputFormat format) {
        Document document = new Document(PageSize.A4, 36, 36, 54, 36);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            addPdfHeader(document, reportType, data);
            addReportContent(document, reportType, data);
        } catch (Exception e) {
            logger.error("Error generando el reporte PDF", e);
            throw new RuntimeException("No se pudo generar el PDF", e);
        } finally {
            if (document.isOpen()) document.close();
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.error("Error cerrando OutputStream", e);
            }
        }
        return outputStream.toByteArray();
    }

    @Override
    public void addPdfHeader(Document doc, ReportingConstants.ReportType type, Map<String, Object> data) {
        try {
            Paragraph title = new Paragraph(type.getTitle(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph sub = new Paragraph("Arrendadora Alberto Jr.", subtitleFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            doc.add(sub);

            Paragraph date = new Paragraph("Fecha de generación: " + LocalDate.now(), italicFont);
            date.setAlignment(Element.ALIGN_RIGHT);
            doc.add(date);

            doc.add(Chunk.NEWLINE);
        } catch (DocumentException e) {
            logger.error("Error al añadir el encabezado", e);
        }
    }

    @Override
    public void addReportContent(Document doc, ReportingConstants.ReportType type, Map<String, Object> data) {
        try {
            switch (type) {
                case MOST_RENTED_VEHICLES -> addMostRentedVehiclesTable(doc, data);
                case RENTAL_TRENDS -> addRentalTrendsTable(doc, data);
                case VEHICLE_USAGE -> addVehicleUsageTable(doc, data);
                case RENTAL_SUMMARY -> addRentalSummary(doc, data);
                case REVENUE_ANALYSIS -> addRevenueAnalysis(doc, data);
                case CUSTOMER_ACTIVITY -> addCustomerActivity(doc, data);
                default -> doc.add(new Paragraph("Contenido no disponible para este tipo de reporte.", normalFont));
            }
        } catch (Exception e) {
            logger.error("Error al añadir contenido del reporte", e);
        }
    }

    private void addRentalSummary(Document doc, Map<String, Object> data) throws DocumentException {
        Paragraph title = new Paragraph("Resumen de Alquileres", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(Chunk.NEWLINE);

        PdfPTable table = createKeyValueTable();
        table.addCell(new Phrase("Total de Alquileres:", boldFont));
        table.addCell(new Phrase(data.get("totalRentals").toString(), normalFont));
        table.addCell(new Phrase("Clientes Únicos:", boldFont));
        table.addCell(new Phrase(data.get("uniqueCustomers").toString(), normalFont));
        table.addCell(new Phrase("Duración Promedio:", boldFont));
        table.addCell(new Phrase(data.get("averageRentalDuration") + " días", italicFont));

        Map<String, Object> mostRented = (Map<String, Object>) data.get("mostRentedVehicle");
        if (mostRented != null) {
            table.addCell(new Phrase("Vehículo más alquilado:", boldFont));
            table.addCell(new Phrase(mostRented.get("brand") + " " + mostRented.get("model") + " (" + mostRented.get("rentalCount") + " veces)", normalFont));
        }

        table.addCell(new Phrase("Ingresos Totales:", boldFont));
        table.addCell(new Phrase("$" + String.format("%.2f", data.get("totalRevenue")), boldItalicFont));

        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    private void addRevenueAnalysis(Document doc, Map<String, Object> data) throws DocumentException {
        Paragraph title = new Paragraph("Análisis de Ingresos", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(Chunk.NEWLINE);

        PdfPTable table = createKeyValueTable();
        table.addCell(new Phrase("Total Ingresos:", boldFont));
        table.addCell(new Phrase("$" + String.format("%.2f", data.get("totalRevenue")), boldFont));

        Number total = (Number) data.get("totalRentals");
        if (total != null && total.longValue() > 0) {
            double avg = ((Number) data.get("totalRevenue")).doubleValue() / total.longValue();
            table.addCell(new Phrase("Promedio por Alquiler:", boldFont));
            table.addCell(new Phrase("$" + String.format("%.2f", avg), italicFont));
        }

        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    private void addCustomerActivity(Document doc, Map<String, Object> data) throws DocumentException {
        Paragraph title = new Paragraph("Actividad de Clientes", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(Chunk.NEWLINE);

        PdfPTable summaryTable = createKeyValueTable();
        summaryTable.addCell(new Phrase("Clientes Activos:", boldFont));
        summaryTable.addCell(new Phrase(data.get("activeCustomers").toString(), normalFont));
        summaryTable.addCell(new Phrase("Nuevos Clientes:", boldFont));
        summaryTable.addCell(new Phrase(data.get("newCustomers").toString(), normalFont));
        doc.add(summaryTable);
        doc.add(Chunk.NEWLINE);

        List<Map<String, Object>> topCustomers = (List<Map<String, Object>>) data.get("topCustomersByRentals");
        Map<String, Double> avgDurationByCustomer = (Map<String, Double>) data.get("averageRentalDurationByTopCustomers");

        if (topCustomers != null && !topCustomers.isEmpty()) {
            Paragraph topCustomersTitle = new Paragraph("Clientes con Mayor Actividad:", headerFont);
            topCustomersTitle.setAlignment(Element.ALIGN_CENTER);
            doc.add(topCustomersTitle);
            doc.add(Chunk.NEWLINE);

            PdfPTable topCustomersTable = new PdfPTable(3); // Nombre, Cantidad, Duración Promedio
            topCustomersTable.setWidthPercentage(80);
            topCustomersTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            addTableHeader(topCustomersTable, "Nombre Cliente");
            addTableHeader(topCustomersTable, "Cantidad Alquileres");
            addTableHeader(topCustomersTable, "Duración Promedio (días)");

            for (Map<String, Object> customer : topCustomers) {
                String customerName = String.valueOf(customer.get("name"));
                topCustomersTable.addCell(new Phrase(customerName, normalFont));
                topCustomersTable.addCell(new Phrase(String.valueOf(customer.get("rentalCount")), normalFont));
                if (avgDurationByCustomer != null && avgDurationByCustomer.containsKey(customerName)) {
                    topCustomersTable.addCell(new Phrase(String.format("%.2f", avgDurationByCustomer.get(customerName)), italicFont));
                } else {
                    topCustomersTable.addCell(new Phrase("-", normalFont)); // Si no hay dato de duración
                }
            }
            doc.add(topCustomersTable);
            doc.add(Chunk.NEWLINE);
        } else {
            doc.add(new Paragraph("No hay datos de actividad de clientes disponibles.", normalFont));
            doc.add(Chunk.NEWLINE);
        }
    }

    private void addMostRentedVehiclesTable(Document doc, Map<String, Object> data) throws DocumentException {
        Map<String, Object> vehicle = (Map<String, Object>) data.get("mostRentedVehicle");
        if (vehicle == null || vehicle.isEmpty()) return;

        Paragraph title = new Paragraph("Vehículo Más Alquilado", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        addTableHeader(table, "Vehículo");
        addTableHeader(table, "Cantidad");

        table.addCell(new Phrase(vehicle.get("brand") + " " + vehicle.get("model"), boldFont));
        table.addCell(new Phrase(vehicle.get("rentalCount").toString(), normalFont));
        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    private void addRentalTrendsTable(Document doc, Map<String, Object> data) throws DocumentException {
        List<Map<String, Object>> trends = (List<Map<String, Object>>) data.get("rentalTrends");
        if (trends == null || trends.isEmpty()) return;

        Paragraph title = new Paragraph("Tendencias de Alquileres", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        addTableHeader(table, "Periodo");
        addTableHeader(table, "Cantidad");

        for (Map<String, Object> t : trends) {
            table.addCell(new Phrase(String.valueOf(t.get("period")), normalFont));
            table.addCell(new Phrase(String.valueOf(t.get("rentalCount")), normalFont));
        }

        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    private void addVehicleUsageTable(Document doc, Map<String, Object> data) throws DocumentException {
        Map<Vehicle, Long> usage = (Map<Vehicle, Long>) data.get("vehicleUsage");
        if (usage == null || usage.isEmpty()) return;

        Paragraph title = new Paragraph("Uso de Vehículos", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        addTableHeader(table, "Vehículo");
        addTableHeader(table, "Usos");

        usage.forEach((v, count) -> {
            table.addCell(new Phrase(v.getBrand() + " " + v.getModel(), normalFont));
            table.addCell(new Phrase(String.valueOf(count), normalFont));
        });

        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    private PdfPTable createKeyValueTable() {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        return table;
    }

    private void addTableHeader(PdfPTable table, String title) {
        PdfPCell cell = new PdfPCell(new Phrase(title, headerFont));
        cell.setBackgroundColor(headerBackgroundColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    @Override
    public String getReportTitle(ReportingConstants.ReportType reportType) {
        return reportType.getTitle() + " (PDF)";
    }
}