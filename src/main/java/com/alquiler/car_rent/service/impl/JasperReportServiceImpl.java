package com.alquiler.car_rent.service.impl;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	private final RentalRepository rentalRepository;
	 public JasperReportServiceImpl(RentalRepository rentalRepository ) {
		 this.rentalRepository = rentalRepository;
	 }

	@Override
	public File generateMonthlyRentalReport() throws IOException {
		List<Rental>rentals = rentalRepository.findAll();
		
		File reportFile = new File ("rental_report.pdf");
		
		try {

            JasperReport jasperReport = JasperCompileManager.compileReport("src/main/resources/reports/rental_report.jrxml");

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rentals);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ReportTitle", "Reporte de Alquileres del Mes");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            FileOutputStream outputStream = new FileOutputStream(reportFile);
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
        } catch (Exception e) {
            throw new IOException("Error generando el reporte", e);
		}
		
		return reportFile;
	}

}
