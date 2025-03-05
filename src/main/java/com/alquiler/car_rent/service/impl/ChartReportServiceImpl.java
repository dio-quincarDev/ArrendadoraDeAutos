package com.alquiler.car_rent.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.service.ChartReportService;

@Service
public class ChartReportServiceImpl implements ChartReportService {
    private static final Logger logger = LoggerFactory.getLogger(ChartReportServiceImpl.class);
    
    private final RentalRepository rentalRepository;

    public ChartReportServiceImpl(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    @Override
    public byte[] generateMostRentedCarChart() throws IOException {
        List<Rental> rentals = rentalRepository.findAll();
        
        if (rentals.isEmpty()) {
            logger.warn("No rental data available for chart generation");
            throw new IOException("No hay datos para generar el gráfico.");
        }

        Map<String, Long> rentalCount = rentals.stream()
            .collect(Collectors.groupingBy(rental -> rental.getVehicle().getModel(), Collectors.counting()));

        logger.info("Generating most rented cars chart with {} unique models", rentalCount.size());

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        rentalCount.forEach((model, count) -> dataset.addValue(count, "Alquileres", model));

        JFreeChart chart = ChartFactory.createBarChart(
            "Más Alquilados", "Modelo", "Veces Alquilado", dataset
        );

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ChartUtils.writeChartAsPNG(outputStream, chart, 800, 600);
            
            byte[] chartBytes = outputStream.toByteArray();
            logger.info("Most rented cars chart generated successfully");
            
            return chartBytes;
        } catch (Exception e) {
            logger.error("Error generating most rented cars chart", e);
            throw new IOException("Error generando el gráfico", e);
        }
    }
}