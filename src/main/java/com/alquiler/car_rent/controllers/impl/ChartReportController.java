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

import com.alquiler.car_rent.controllers.ChartReportApi;
import com.alquiler.car_rent.service.ChartReportService;

@RestController
public class ChartReportController implements ChartReportApi {
    private static final Logger logger = LoggerFactory.getLogger(ChartReportController.class);

    private final ChartReportService chartReportService;

    public ChartReportController(ChartReportService chartReportService) {
        this.chartReportService = chartReportService;
    }

    @Override
    public ResponseEntity<byte[]> getMostRentedCarChart() {
        try {
            return generateChartResponse(false);
        } catch (Exception e) {
            logger.error("Error generating most rented cars chart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Error generating chart: " + e.getMessage()).getBytes());
        }
    }

    @Override
    public ResponseEntity<byte[]> downloadMostRentedCarChart() {
        try {
            return generateChartResponse(true); // Llama a generateChartResponse con isDownload = true
        } catch (Exception e) {
            logger.error("Error downloading most rented cars chart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Error downloading chart: " + e.getMessage()).getBytes());
        }
    }

    private ResponseEntity<byte[]> generateChartResponse(boolean isDownload) throws IOException {
        byte[] imageBytes = chartReportService.generateMostRentedCarChart();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "image/png");
        headers.setCacheControl(CacheControl.noCache());

        if (isDownload) {
            headers.setContentDisposition(
                ContentDisposition.attachment().filename("most_rented_cars.png").build()
            );
        } else {
            headers.setContentDisposition(
                ContentDisposition.inline().filename("most_rented_cars.png").build()
            );
        }

        return ResponseEntity.ok()
            .headers(headers)
            .body(imageBytes);
    }
}