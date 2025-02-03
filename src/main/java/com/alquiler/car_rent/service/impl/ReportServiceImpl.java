package com.alquiler.car_rent.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.service.ReportService;

@Service
public class ReportServiceImpl implements ReportService {
	
	private final RentalRepository rentalRepository;
	
	public ReportServiceImpl(RentalRepository rentalRepository) {
		this.rentalRepository = rentalRepository;
	}

	@Override
	public File generatedMostRentedCarChart() throws IOException {
		List<Rental> rentals = rentalRepository.findAll();

	
	
	Map<String, Long> rentalCount = rentals.stream()
			.collect(Collectors.groupingBy(rental-> rental.getVehicle().getModel(), Collectors.counting()));
	
	DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		rentalCount.forEach((model, count)-> dataset.addValue(count, "Alquileres", model));
		
	JFreeChart chart = ChartFactory.createBarChart(
			"Mas Alquilados", "Modelo", "Veces Alquilado", dataset
			);
	
	File chartFile = new File("most_rented_cars.png");
	ChartUtils.saveChartAsPNG(chartFile, chart, 800, 600);
	
	return chartFile;
	
			
	}
}


