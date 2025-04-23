package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.service.reportService.PdfReportService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
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
    private static final Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font normalFont = new Font(Font.FontFamily.HELVETICA, 10);
    private static final Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);

    @Override
    public void addPdfHeader(Document doc, ReportingConstants.ReportType type, Map<String, Object> data) {
        try {
            Paragraph title = new Paragraph(type.getTitle(), titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            doc.add(title);

            Paragraph date = new Paragraph("Fecha de Generación: " + LocalDate.now().toString(), dateFont);
            date.setAlignment(Paragraph.ALIGN_RIGHT);
            doc.add(date);

            Paragraph spacing = new Paragraph(" ");
            doc.add(spacing);

        } catch (DocumentException e) {
            logger.error("Error al añadir el encabezado del PDF: {}", e.getMessage(), e);
        }
    }

    @Override
    public void addReportContent(Document doc, ReportingConstants.ReportType type, Map<String, Object> data) {
        try {
            switch (type) {
                case MOST_RENTED_VEHICLES:
                    addMostRentedVehiclesTable(doc, data);
                    break;
                case RENTAL_TRENDS:
                    addRentalTrendsTable(doc, data);
                    break;
                case VEHICLE_USAGE:
                    addVehicleUsageList(doc, data);
                    break;
                case RENTAL_SUMMARY:
                    addRentalSummary(doc, data);
                    break;
                case REVENUE_ANALYSIS:
                    addRevenueAnalysis(doc, data);
                    break;
                case CUSTOMER_ACTIVITY:
                    addCustomerActivity(doc, data);
                    break;
                default:
                    Paragraph notSupported = new Paragraph("Contenido no soportado para este tipo de reporte.", normalFont);
                    doc.add(notSupported);
                    break;
            }
        } catch (DocumentException e) {
            logger.error("Error al añadir el contenido del PDF: {}", e.getMessage(), e);
        }
    }

    private void addRentalSummary(Document doc, Map<String, Object> data) throws DocumentException {
        Paragraph title = new Paragraph("Resumen de Alquileres", headerFont);
        doc.add(title);
        doc.add(new Paragraph("Total de Alquileres: " + data.get("totalRentals"), normalFont));
        doc.add(new Paragraph("Clientes Únicos que Alquilaron: " + data.get("uniqueCustomers"), normalFont));
        doc.add(new Paragraph("Duración Promedio de Alquiler: " + String.format("%.2f", data.get("averageRentalDuration")) + " días", normalFont));
        Map<String, Object> mostRented = (Map<String, Object>) data.get("mostRentedVehicle");
        if (mostRented != null) {
            doc.add(new Paragraph("Vehículo Más Alquilado: " + mostRented.get("brand") + " " + mostRented.get("model") + " (" + mostRented.get("rentalCount") + " veces)", normalFont));
        }
        doc.add(new Paragraph("Ingresos Totales por Alquileres: $" + String.format("%.2f", data.get("totalRevenue")), normalFont));
        doc.add(new Paragraph(" "));
    }

    private void addRevenueAnalysis(Document doc, Map<String, Object> data) throws DocumentException {
        Paragraph title = new Paragraph("Análisis de Ingresos", headerFont);
        doc.add(title);
        doc.add(new Paragraph("Ingresos Totales por Alquileres: $" + String.format("%.2f", data.get("totalRevenue")), normalFont));
        doc.add(new Paragraph("Ingresos Promedio por Alquiler: $" + String.format("%.2f", (Double) data.get("totalRevenue") / (Long) data.get("totalRentals")), normalFont));
        List<Map<String, Object>> rentalTrends = (List<Map<String, Object>>) data.get("rentalTrends");
        if (rentalTrends != null && !rentalTrends.isEmpty()) {
            Paragraph trendsTitle = new Paragraph("Tendencias de Ingresos (Estimado):", headerFont);
            doc.add(trendsTitle);
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            addTableHeader(table, "Período");
            addTableHeader(table, "Ingresos Estimados");
            double averageRate = (Double) data.getOrDefault("averageRentalRate", 50.0); // Tarifa promedio por día (ejemplo)
            rentalTrends.forEach(trend -> {
                table.addCell(new Phrase(String.valueOf(trend.get("period")), normalFont));
                table.addCell(new Phrase("$" + String.format("%.2f", (Long) trend.get("rentalCount") * averageRate), normalFont));
            });
            doc.add(table);
        }
        doc.add(new Paragraph(" "));
    }

    private void addCustomerActivity(Document doc, Map<String, Object> data) throws DocumentException {
        Paragraph title = new Paragraph("Actividad de Clientes", headerFont);
        doc.add(title);
        doc.add(new Paragraph("Total de Clientes Activos: " + data.get("activeCustomers"), normalFont));
        doc.add(new Paragraph("Nuevos Clientes: " + data.get("newCustomers"), normalFont));

        List<Map<String, Object>> topCustomers = (List<Map<String, Object>>) data.get("topCustomersByRentals");
        if (topCustomers != null && !topCustomers.isEmpty()) {
            Paragraph topCustomersTitle = new Paragraph("Clientes con Mayor Actividad:", headerFont);
            doc.add(topCustomersTitle);
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            addTableHeader(table, "ID Cliente");
            addTableHeader(table, "Nombre Cliente");
            addTableHeader(table, "Cantidad de Alquileres");
            topCustomers.forEach(customer -> {
                table.addCell(new Phrase(String.valueOf(customer.get("customerId")), normalFont));
                table.addCell(new Phrase(String.valueOf(customer.get("name")), normalFont));
                table.addCell(new Phrase(String.valueOf(customer.get("rentalCount")), normalFont));
            });
            doc.add(table);

            Map<String, Double> avgDurationByCustomer = (Map<String, Double>) data.get("averageRentalDurationByTopCustomers");
            if (avgDurationByCustomer != null && !avgDurationByCustomer.isEmpty()) {
                Paragraph avgDurationTitle = new Paragraph("Duración Promedio de Alquiler por Cliente (Top " + topCustomers.size() + "):", headerFont);
                doc.add(avgDurationTitle);
                com.itextpdf.text.List list = new com.itextpdf.text.List(false); // Lista no ordenada
                avgDurationByCustomer.forEach((name, duration) -> {
                    list.add(new ListItem(name + ": " + String.format("%.2f", duration) + " días", normalFont));
                });
                doc.add(list);
            }
        } else {
            Paragraph noData = new Paragraph("No hay datos de actividad de clientes disponibles.", normalFont);
            doc.add(noData);
        }
        doc.add(new Paragraph(" "));
    }

    private void addMostRentedVehiclesTable(Document doc, Map<String, Object> data) throws DocumentException {
        Map<Vehicle, Long> rentalCounts = (Map<Vehicle, Long>) data.get("rentalCountsByVehicle");
        if (rentalCounts != null && !rentalCounts.isEmpty()) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            addTableHeader(table, "Vehículo");
            addTableHeader(table, "Cantidad de Alquileres");

            rentalCounts.entrySet().stream()
                    .sorted(Map.Entry.<Vehicle, Long>comparingByValue().reversed())
                    .forEach(entry -> {
                        table.addCell(new Phrase(entry.getKey().getBrand() + " " + entry.getKey().getModel(), normalFont));
                        table.addCell(new Phrase(String.valueOf(entry.getValue()), normalFont));
                    });
            doc.add(table);
        } else {
            Paragraph noData = new Paragraph("No hay datos disponibles para los vehículos más alquilados.", normalFont);
            doc.add(noData);
        }
    }

    private void addRentalTrendsTable(Document doc, Map<String, Object> data) throws DocumentException {
        List<Map<String, Object>> rentalTrends = (List<Map<String, Object>>) data.get("rentalTrends");
        if (rentalTrends != null && !rentalTrends.isEmpty()) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            addTableHeader(table, "Período");
            addTableHeader(table, "Cantidad de Alquileres");

            rentalTrends.forEach(trend -> {
                table.addCell(new Phrase(String.valueOf(trend.get("period")), normalFont));
                table.addCell(new Phrase(String.valueOf(trend.get("rentalCount")), normalFont));
            });
            doc.add(table);
        } else {
            Paragraph noData = new Paragraph("No hay datos disponibles para las tendencias de alquileres.", normalFont);
            doc.add(noData);
        }
    }

    private void addVehicleUsageList(Document doc, Map<String, Object> data) throws DocumentException {
        Map<Vehicle, Long> usage = (Map<Vehicle, Long>) data.get("vehicleUsage");
        if (usage != null && !usage.isEmpty()) {
            Paragraph title = new Paragraph("Uso de Vehículos:", headerFont);
            doc.add(title);
            com.itextpdf.text.List list = new com.itextpdf.text.List(true); // El argumento 'true' indica una lista ordenada
            usage.forEach((vehicle, count) -> {
                list.add(new ListItem(vehicle.getBrand() + " " + vehicle.getModel() + ": " + count + " usos", normalFont));
            });
            doc.add(list);
        } else {
            Paragraph noData = new Paragraph("No hay datos disponibles para el uso de vehículos.", normalFont);
            doc.add(noData);
        }
    }

    private void addTableHeader(PdfPTable table, String header) {
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        headerCell.setBorderWidth(1);
        headerCell.setPhrase(new Phrase(header, headerFont));
        table.addCell(headerCell);
    }

    @Override
    public byte[] generateReport(Map<String, Object> data, ReportingConstants.ReportType reportType, ReportingConstants.OutputFormat format) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = null;

        try {
            writer = PdfWriter.getInstance(document, outputStream);
            document.open();

            addPdfHeader(document, reportType, data);
            addReportContent(document, reportType, data);

            document.close();
            return outputStream.toByteArray();

        } catch (DocumentException e) {
            logger.error("Error al crear el documento PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el reporte PDF", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.error("Error al cerrar el OutputStream: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public String getReportTitle(ReportingConstants.ReportType reportType) {
        return reportType.getTitle() + " (PDF)";
    }
}