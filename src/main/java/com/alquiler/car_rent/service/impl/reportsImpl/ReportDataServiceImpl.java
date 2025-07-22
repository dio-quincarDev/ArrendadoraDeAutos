
package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.entities.Vehicle;
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

        // Rango seguro: años válidos de 1900 a 2150
        LocalDate safeStart = LocalDate.of(1900, 1, 1);
        LocalDate safeEnd = LocalDate.of(2150, 1, 1);

        LocalDate start;
        LocalDate end;

        if (timePeriod == ReportingConstants.TimePeriod.ALL_TIME) {
            start = (startDate != null && isSafeYear(startDate)) ? startDate : safeStart;
            end = (endDate != null && isSafeYear(endDate)) ? endDate : safeEnd;
        } else if (timePeriod != null) {
            start = (startDate != null && isSafeYear(startDate)) ? startDate : LocalDate.now().minus(timePeriod.getValue(), timePeriod.getUnit());
            end = (endDate != null && isSafeYear(endDate)) ? endDate : LocalDate.now();
        } else {
            start = (startDate != null && isSafeYear(startDate)) ? startDate : LocalDate.now().minusMonths(1);
            end = (endDate != null && isSafeYear(endDate)) ? endDate : LocalDate.now();
        }

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay(); // Inclusivo

        Pair<LocalDateTime, LocalDateTime> dateRange = Pair.of(startDateTime, endDateTime);
        List<Rental> rentals = getRentalsInRange(dateRange.getFirst(), dateRange.getSecond());

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("period", timePeriod != null ? timePeriod.name() : null);
        reportData.put("startDate", start);
        reportData.put("endDate", end);

        addBasicMetrics(reportData, rentals);
        addCustomerMetrics(reportData, timePeriod, start, end);
        addVehicleMetrics(reportData, timePeriod, start, end);
        addVehicleTypeMetrics(reportData, timePeriod, start, end);
        addPricingTierMetrics(reportData, timePeriod, start, end);
        addTrendsAndAdvancedMetrics(reportData, timePeriod, start, end);

        return reportData;
    }

    private boolean isSafeYear(LocalDate date) {
        int year = date.getYear();
        return year >= 1900 && year <= 2150;
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
        reportData.put("customerActivity",metricsService.getCustomerActivity(period, start, end));

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
        Map<Vehicle, Long> rawUsage = metricsService.getVehicleUsage(period, start, end);

        List<Map<String, Object>> usageList = rawUsage.entrySet().stream()
            .map(entry -> {
                Map<String, Object> map = new HashMap<>();
                map.put("vehicle", entry.getKey().getBrand() + " " + entry.getKey().getModel());
                map.put("count", entry.getValue());
                return map;
            })
            .collect(Collectors.toList());

        reportData.put("vehicleUsage", usageList);
        reportData.put("mostRentedVehicle", metricsService.getMostRentedVehicle(period, start, end));
        reportData.put("availableVehicles", metricsService.getAvailableVehiclesCount());

    }

    private void addTrendsAndAdvancedMetrics(Map<String, Object> reportData, ReportingConstants.TimePeriod period, LocalDate start, LocalDate end) {
        reportData.put("rentalTrends", metricsService.getRentalTrends(period, start, end));
        double avgDuration = metricsService.getAverageRentalDuration(period, start, end);
        reportData.put("averageRentalDuration", Math.round(avgDuration));
    }

    private void addVehicleTypeMetrics(Map<String, Object> reportData, ReportingConstants.TimePeriod period, LocalDate start, LocalDate end) {
        reportData.put("rentalsByVehicleType", metricsService.getRentalsCountByVehicleType(period, start, end));
        reportData.put("revenueByVehicleType", metricsService.getRevenueByVehicleType(period, start, end));
    }

    private void addPricingTierMetrics(Map<String, Object> reportData, ReportingConstants.TimePeriod period, LocalDate start, LocalDate end) {
        reportData.put("rentalsByPricingTier", metricsService.getRentalsCountByPricingTier(period, start, end));
        reportData.put("revenueByPricingTier", metricsService.getRevenueByPricingTier(period, start, end));
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