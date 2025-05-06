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
    private static final Font boldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private static final Font italicFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
    private static final BaseColor headerBackgroundColor = BaseColor.LIGHT_GRAY;

    @Override
    public void addPdfHeader(Document doc, ReportingConstants.ReportType type, Map<String, Object> data) {
        try {
            Paragraph title = new Paragraph(type.getTitle(), titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            doc.add(title);

            Paragraph date = new Paragraph("Fecha de Generaci\u00f3n: " + LocalDate.now().toString(), italicFont);
            date.setAlignment(Paragraph.ALIGN_RIGHT);
            doc.add(date);

            doc.add(new Paragraph(" "));

        } catch (DocumentException e) {
            logger.error("Error al a\u00f1adir el encabezado del PDF: {}", e.getMessage(), e);
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
                    doc.add(new Paragraph("Contenido no soportado para este tipo de reporte.", normalFont));
                    break;
            }
        } catch (DocumentException e) {
            logger.error("Error al a\u00f1adir el contenido del PDF: {}", e.getMessage(), e);
        }
    }

    @Override
    public byte[] generateReport(Map<String, Object> data, ReportingConstants.ReportType reportType, ReportingConstants.OutputFormat format) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, outputStream);
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

    private void addRentalSummary(Document doc, Map<String, Object> data) throws DocumentException {
        doc.add(new Paragraph("Resumen de Alquileres", headerFont));
        doc.add(new Paragraph("Total de Alquileres: ", boldFont));
        doc.add(new Paragraph(data.get("totalRentals").toString(), normalFont));
        doc.add(new Paragraph("Clientes \u00danicos que Alquilaron: ", boldFont));
        doc.add(new Paragraph(data.get("uniqueCustomers").toString(), normalFont));
        doc.add(new Paragraph("Duraci\u00f3n Promedio de Alquiler: ", boldFont));
        doc.add(new Paragraph(String.format("%.2f", data.get("averageRentalDuration")) + " d\u00edas", normalFont));
        Map<String, Object> mostRented = (Map<String, Object>) data.get("mostRentedVehicle");
        if (mostRented != null) {
            doc.add(new Paragraph("Veh\u00edculo M\u00e1s Alquilado: ", boldFont));
            doc.add(new Paragraph(mostRented.get("brand") + " " + mostRented.get("model") + " (" + mostRented.get("rentalCount") + " veces)", normalFont));
        }
        doc.add(new Paragraph("Ingresos Totales por Alquileres: ", boldFont));
        doc.add(new Paragraph("$" + String.format("%.2f", data.get("totalRevenue")), normalFont));
        doc.add(new Paragraph(" "));
    }

    private void addRevenueAnalysis(Document doc, Map<String, Object> data) throws DocumentException {
        doc.add(new Paragraph("An\u00e1lisis de Ingresos", headerFont));
        doc.add(new Paragraph("Ingresos Totales por Alquileres: ", boldFont));
        doc.add(new Paragraph("$" + String.format("%.2f", data.get("totalRevenue")), normalFont));
        if (data.containsKey("totalRevenue") && data.get("totalRevenue") instanceof Number &&
                data.containsKey("totalRentals") && data.get("totalRentals") instanceof Number &&
                ((Number) data.get("totalRentals")).longValue() > 0) {
            doc.add(new Paragraph("Ingresos Promedio por Alquiler: ", boldFont));
            doc.add(new Paragraph("$" + String.format("%.2f", ((Number) data.get("totalRevenue")).doubleValue() / ((Number) data.get("totalRentals")).doubleValue()), normalFont));
        }

        List<Map<String, Object>> rentalTrends = (List<Map<String, Object>>) data.get("rentalTrends");
        if (rentalTrends != null && !rentalTrends.isEmpty()) {
            doc.add(new Paragraph("Tendencias de Ingresos (Estimado):", headerFont));
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            addTableHeader(table, "Per\u00edodo");
            addTableHeader(table, "Ingresos Estimados");
            double averageRate = (Double) data.getOrDefault("averageRentalRate", 50.0);
            for (Map<String, Object> trend : rentalTrends) {
                table.addCell(new Phrase(String.valueOf(trend.get("period")), normalFont));
                table.addCell(new Phrase("$" + String.format("%.2f", (Long) trend.get("rentalCount") * averageRate), normalFont));
            }
            doc.add(table);
        }
        doc.add(new Paragraph(" "));
    }

    private void addCustomerActivity(Document doc, Map<String, Object> data) throws DocumentException {
        doc.add(new Paragraph("Actividad de Clientes", headerFont));
        doc.add(new Paragraph("Total de Clientes Activos: ", boldFont));
        doc.add(new Paragraph(data.get("activeCustomers").toString(), normalFont));
        doc.add(new Paragraph("Nuevos Clientes: ", boldFont));
        doc.add(new Paragraph(data.get("newCustomers").toString(), normalFont));

        List<Map<String, Object>> topCustomers = (List<Map<String, Object>>) data.get("topCustomersByRentals");
        if (topCustomers != null && !topCustomers.isEmpty()) {
            doc.add(new Paragraph("Clientes con Mayor Actividad:", headerFont));
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            addTableHeader(table, "ID Cliente");
            addTableHeader(table, "Nombre Cliente");
            addTableHeader(table, "Cantidad de Alquileres");
            for (Map<String, Object> customer : topCustomers) {
                table.addCell(new Phrase(String.valueOf(customer.get("customerId")), normalFont));
                table.addCell(new Phrase(String.valueOf(customer.get("name")), normalFont));
                table.addCell(new Phrase(String.valueOf(customer.get("rentalCount")), normalFont));
            }
            doc.add(table);

            Map<String, Double> avgDurationByCustomer = (Map<String, Double>) data.get("averageRentalDurationByTopCustomers");
            if (avgDurationByCustomer != null && !avgDurationByCustomer.isEmpty()) {
                doc.add(new Paragraph("Duraci\u00f3n Promedio de Alquiler por Cliente (Top " + topCustomers.size() + "):", headerFont));
                com.itextpdf.text.List list = new com.itextpdf.text.List(false);
                avgDurationByCustomer.forEach((name, duration) -> {
                    list.add(new ListItem(name + ": " + String.format("%.2f", duration) + " d\u00edas", normalFont));
                });
                doc.add(list);
            }
        } else {
            doc.add(new Paragraph("No hay datos de actividad de clientes disponibles.", normalFont));
        }
        doc.add(new Paragraph(" "));
    }

    private void addMostRentedVehiclesTable(Document doc, Map<String, Object> data) throws DocumentException {
        Map<Vehicle, Long> rentalCounts = (Map<Vehicle, Long>) data.get("rentalCountsByVehicle");
        if (rentalCounts != null && !rentalCounts.isEmpty()) {
            doc.add(new Paragraph("Veh\u00edculos M\u00e1s Alquilados:", headerFont));
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            addTableHeader(table, "Veh\u00edculo");
            addTableHeader(table, "Cantidad de Alquileres");
            rentalCounts.entrySet().stream()
                    .sorted(Map.Entry.<Vehicle, Long>comparingByValue().reversed())
                    .forEach(entry -> {
                        table.addCell(new Phrase(entry.getKey().getBrand() + " " + entry.getKey().getModel(), normalFont));
                        table.addCell(new Phrase(String.valueOf(entry.getValue()), normalFont));
                    });
            doc.add(table);
        } else {
            doc.add(new Paragraph("No hay datos disponibles para los veh\u00edculos m\u00e1s alquilados.", normalFont));
        }
        doc.add(new Paragraph(" "));
    }

    private void addRentalTrendsTable(Document doc, Map<String, Object> data) throws DocumentException {
        List<Map<String, Object>> rentalTrends = (List<Map<String, Object>>) data.get("rentalTrends");
        if (rentalTrends != null && !rentalTrends.isEmpty()) {
            doc.add(new Paragraph("Tendencias de Alquileres:", headerFont));
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            addTableHeader(table, "Per\u00edodo");
            addTableHeader(table, "Cantidad de Alquileres");
            for (Map<String, Object> trend : rentalTrends) {
                table.addCell(new Phrase(String.valueOf(trend.get("period")), normalFont));
                table.addCell(new Phrase(String.valueOf(trend.get("rentalCount")), normalFont));
            }
            doc.add(table);
        } else {
            doc.add(new Paragraph("No hay datos disponibles para las tendencias de alquileres.", normalFont));
        }
        doc.add(new Paragraph(" "));
    }

    private void addVehicleUsageList(Document doc, Map<String, Object> data) throws DocumentException {
        Map<Vehicle, Long> usage = (Map<Vehicle, Long>) data.get("vehicleUsage");
        if (usage != null && !usage.isEmpty()) {
            doc.add(new Paragraph("Uso de Veh\u00edculos:", headerFont));
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            addTableHeader(table, "Veh\u00edculo");
            addTableHeader(table, "Cantidad de Usos");
            usage.entrySet().stream()
                    .sorted(Map.Entry.<Vehicle, Long>comparingByValue().reversed())
                    .forEach(entry -> {
                        table.addCell(new Phrase(entry.getKey().getBrand() + " " + entry.getKey().getModel(), normalFont));
                        table.addCell(new Phrase(String.valueOf(entry.getValue()), normalFont));
                    });
            doc.add(table);
        } else {
            doc.add(new Paragraph("No hay datos disponibles para el uso de veh\u00edculos.", normalFont));
        }
        doc.add(new Paragraph(" "));
    }

    private void addTableHeader(PdfPTable table, String header) {
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(headerBackgroundColor);
        headerCell.setBorderWidth(1);
        headerCell.setPhrase(new Phrase(header, headerFont));
        table.addCell(headerCell);
    }
}