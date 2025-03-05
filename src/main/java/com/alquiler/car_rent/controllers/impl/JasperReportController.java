package com.alquiler.car_rent.controllers.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.alquiler.car_rent.controllers.JasperReportApi;
import com.alquiler.car_rent.service.JasperReportService;

@RestController
public class JasperReportController implements JasperReportApi {
    private static final Logger logger = LoggerFactory.getLogger(JasperReportController.class);

    private final JasperReportService jasperReportService;

    public JasperReportController(JasperReportService jasperReportService) {
        this.jasperReportService = jasperReportService;
    }

    @Override
    public ResponseEntity<byte[]> getMonthlyRentalReports() {
        try {
            return generatePdfResponse(false);
        } catch (Exception e) {
            logger.error("Error generating monthly rental report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Error generating report: " + e.getMessage()).getBytes());
        }
    }

    @Override
    public ResponseEntity<byte[]> downloadMonthlyRentalReport() {
        try {
            return generatePdfResponse(true); // Llama a generatePdfResponse con isDownload = true
        } catch (Exception e) {
            logger.error("Error downloading monthly rental report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Error downloading report: " + e.getMessage()).getBytes());
        }
    }

    private ResponseEntity<byte[]> generatePdfResponse(boolean isDownload) throws IOException {
        byte[] pdfBytes = jasperReportService.generateMonthlyRentalReport();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/pdf");
        headers.setCacheControl(CacheControl.noCache());

        if (isDownload) {
            headers.setContentDisposition(
                ContentDisposition.attachment().filename("rental_report.pdf").build()
            );
        } else {
            headers.setContentDisposition(
                ContentDisposition.inline().filename("rental_report.pdf").build()
            );
        }

        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
    }
}