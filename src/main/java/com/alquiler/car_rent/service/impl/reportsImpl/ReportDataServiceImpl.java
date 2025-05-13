
package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.service.reportService.ExcelReportService;
import com.alquiler.car_rent.service.reportService.MetricsService;
import com.alquiler.car_rent.service.reportService.ReportDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportDataServiceImpl implements ReportDataService {

    private static final Logger logger = LoggerFactory.getLogger(ReportDataServiceImpl.class);
    private static final int DEFAULT_TOP_CUSTOMERS_LIMIT = 10;

    private final RentalRepository rentalRepository;
    private final MetricsService metricsService;
    private final ExcelReportService excelReportService;

    @Value("${reporting.page.size:100}")
    private int pageSize;

    public ReportDataServiceImpl(RentalRepository rentalRepository,
                                 MetricsService metricsService,
                                 ExcelReportService excelReportService) {
        this.rentalRepository = rentalRepository;
        this.metricsService = metricsService;
        this.excelReportService = excelReportService;
    }

    @Override
    public Map<String, Object> generateReportData(ReportingConstants.TimePeriod timePeriod,
                                                  LocalDate startDate,
                                                  LocalDate endDate) {
        logger.info("Generando datos del reporte para el período: {}, startDate: {}, endDate: {}", timePeriod, startDate, endDate);

        LocalDate start;
        LocalDate end;

        if (timePeriod != null) {
            start = Optional.ofNullable(startDate).orElse(LocalDate.now().minus(timePeriod.getValue(), timePeriod.getUnit()));
            end = Optional.ofNullable(endDate).orElse(LocalDate.now());
        } else {
            // Lógica por defecto cuando timePeriod es null
            start = Optional.ofNullable(startDate).orElse(LocalDate.now().minusMonths(1)); // Ejemplo: Último mes
            end = Optional.ofNullable(endDate).orElse(LocalDate.now());
        }

        Pair<LocalDateTime, LocalDateTime> dateRange = Pair.of(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
        List<Rental> rentals = getRentalsInRange(dateRange.getFirst(), dateRange.getSecond());

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("period", timePeriod);
        reportData.put("startDate", start);
        reportData.put("endDate", end);

        addBasicMetrics(reportData, rentals);
        addCustomerMetrics(reportData, timePeriod, start, end);
        addVehicleMetrics(reportData, timePeriod, start, end);
        addTrendsAndAdvancedMetrics(reportData, timePeriod, start, end);

        return reportData;
    }

    private void addBasicMetrics(Map<String, Object> reportData, List<Rental> rentals) {
        reportData.put("totalRentals", rentals.size());
        reportData.put("totalRevenue", calculateTotalRevenue(rentals));
    }

    private double calculateTotalRevenue(List<Rental> rentals) {
        return rentals.stream()
                .mapToDouble(r -> r.getTotalPrice().doubleValue())
                .sum();
    }

    private void addCustomerMetrics(Map<String, Object> reportData, ReportingConstants.TimePeriod period, LocalDate start, LocalDate end) {
        reportData.put("uniqueCustomers", metricsService.getUniqueCustomersRented(period, start, end));
        reportData.put("activeCustomers", metricsService.getActiveCustomersCount(period, start, end));
        reportData.put("newCustomers", metricsService.getNewCustomersCount(period, start, end));

        List<Map<String, Object>> topCustomers = metricsService.getTopCustomersByRentals(period, start, end, DEFAULT_TOP_CUSTOMERS_LIMIT);
        List<Map<String, Object>> topCustomersSanitized = topCustomers.stream()
                .map(c -> Map.of(
                        "name", c.get("name"),
                        "rentalCount", c.get("rentalCount")
                ))
                .collect(Collectors.toList());

        reportData.put("topCustomersByRentals", topCustomersSanitized);

        if (!topCustomers.isEmpty()) {
            List<Long> customerIds = topCustomers.stream()
                    .map(c -> (Long) c.get("customerId"))
                    .collect(Collectors.toList());

            Map<String, Double> avgDuration = metricsService.getAverageRentalDurationByCustomer(period, start, end, customerIds);
            reportData.put("averageRentalDurationByTopCustomers", avgDuration);
        }
    }

    private void addVehicleMetrics(Map<String, Object> reportData, ReportingConstants.TimePeriod period, LocalDate start, LocalDate end) {
        reportData.put("vehicleUsage", metricsService.getVehicleUsage(period, start, end));
        reportData.put("mostRentedVehicle", metricsService.getMostRentedVehicle(period, start, end));
    }

    private void addTrendsAndAdvancedMetrics(Map<String, Object> reportData, ReportingConstants.TimePeriod period, LocalDate start, LocalDate end) {
        reportData.put("rentalTrends", metricsService.getRentalTrends(period, start, end));
        double avgDuration = metricsService.getAverageRentalDuration(period, start, end);
        reportData.put("averageRentalDuration", Math.round(avgDuration));
    }

    @Override
    public List<Rental> getRentalsInRange(LocalDateTime start, LocalDateTime end) {
        List<Rental> allRentals = new ArrayList<>();
        int pageNum = 0;
        Page<Rental> page;
        do {
            page = rentalRepository.searchByDateRange(start, end, PageRequest.of(pageNum++, pageSize));
            allRentals.addAll(page.getContent());
        } while (page.hasNext());
        return allRentals;
    }

    @Override
    public LocalDateTime toDateTime(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    @Override
    public String formatDate(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }

    @Override
    public byte[] generateGenericTableExcel(List<String> headers, List<List<String>> data) {
        return excelReportService.generateGenericTableExcel(headers, data);
    }
}