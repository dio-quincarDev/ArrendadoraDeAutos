package com.alquiler.car_rent.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.service.JasperReportService;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class JasperReportServiceImpl implements JasperReportService {
    private static final Logger logger = LoggerFactory.getLogger(JasperReportServiceImpl.class);
    
    private final RentalRepository rentalRepository;

    public JasperReportServiceImpl(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    @Override
    public byte[] generateMonthlyRentalReport() throws IOException {
        List<Rental> rentals = rentalRepository.findAll();
        
        if (rentals.isEmpty()) {
            logger.warn("No rental data available for report generation");
            throw new IOException("No hay datos para generar el reporte.");
        }

        try (InputStream reportStream = new ClassPathResource("reports/rental_report.jrxml").getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            logger.info("Generating monthly rental report for {} rentals", rentals.size());
            
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rentals);
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ReportTitle", "Reporte de Alquileres del Mes");
            
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            
            byte[] reportBytes = outputStream.toByteArray();
            logger.info("Monthly rental report generated successfully");
            
            return reportBytes;
        } catch (Exception e) {
            logger.error("Error generating monthly rental report", e);
            throw new IOException("Error generando el reporte", e);
        }
    }
}