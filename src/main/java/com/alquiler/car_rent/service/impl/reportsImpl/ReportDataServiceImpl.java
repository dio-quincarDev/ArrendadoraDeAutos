package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.service.reportService.ExcelReportGenericTable;
import com.alquiler.car_rent.service.reportService.MetricsService; // Import MetricsService
import com.alquiler.car_rent.service.reportService.ReportDataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportDataServiceImpl implements ReportDataService {
    private final RentalRepository rentalRepository;
    private final MetricsService metricsService;
    private final ExcelReportGenericTable excelReportGenericTable;

    @Value("${reporting.page.size:100}")
    private int pageSize;

    public ReportDataServiceImpl(RentalRepository rentalRepository, MetricsService metricsService, ExcelReportGenericTable excelReportGenericTable) {
        this.rentalRepository = rentalRepository;
        this.metricsService = metricsService;
        this.excelReportGenericTable = excelReportGenericTable;
    }

    @Override
    public Map<String, Object> generateReportData(ReportingConstants.TimePeriod timePeriod,
                                                  LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, timePeriod);
        LocalDate start = startDate != null ? startDate : dateRange.getFirst().toLocalDate();
        LocalDate end = endDate != null ? endDate : dateRange.getSecond().toLocalDate().minusDays(1);

        List<Rental> rentals = getRentalsInRange(dateRange.getFirst(), dateRange.getSecond());

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("startDate", start);
        reportData.put("endDate", end);
        reportData.put("period", timePeriod);
        reportData.put("totalRentals", (long) rentals.size());
        reportData.put("rentals", rentals);

        double totalRevenue = rentals.stream()
                .mapToDouble(rental -> rental.getTotalPrice().doubleValue())
                .sum();
        reportData.put("totalRevenue", totalRevenue);

        // Métricas adicionales utilizando MetricsService
        reportData.put("uniqueCustomers", metricsService.getUniqueCustomersRented(start, end));
        reportData.put("averageRentalDuration", metricsService.getAverageRentalDuration(start, end));
        reportData.put("mostRentedVehicle", metricsService.getMostRentedVehicle(start, end));
        reportData.put("rentalTrends", metricsService.getRentalTrends(timePeriod, start, end));
        reportData.put("activeCustomers", metricsService.getActiveCustomersCount(start, end));
        reportData.put("newCustomers", metricsService.getNewCustomersCount(start, end));
        reportData.put("topCustomersByRentals", metricsService.getTopCustomersByRentals(start, end, 10)); // Limit to top 10
        List<Map<String, Object>> topCustomers = (List<Map<String, Object>>) reportData.get("topCustomersByRentals");
        if (topCustomers != null && !topCustomers.isEmpty()) {
            List<Long> topCustomerIds = topCustomers.stream()
                    .map(customer -> (Long) customer.get("customerId"))
                    .collect(Collectors.toList());
            reportData.put("averageRentalDurationByTopCustomers", metricsService.getAverageRentalDurationByCustomer(start, end, topCustomerIds));
        } else {
            reportData.put("averageRentalDurationByTopCustomers", new HashMap<>());
        }
        // Para Vehicle Usage y Most Rented Vehicles, podríamos usar la lista de rentals directamente o llamar a MetricsService
        reportData.put("vehicleUsage", rentals.stream()
                .collect(Collectors.groupingBy(Rental::getVehicle, Collectors.counting())));
        reportData.put("rentalCountsByVehicle", rentals.stream()
                .collect(Collectors.groupingBy(Rental::getVehicle, Collectors.counting())));

        return reportData;
    }

    @Override
    public List<Rental> getRentalsInRange(LocalDateTime start, LocalDateTime end) {
        int pageNum = 0;
        List<Rental> allRentals = new ArrayList<>();
        Page<Rental> page;

        do {
            page = rentalRepository.search(start, end, PageRequest.of(pageNum++, pageSize));
            allRentals.addAll(page.getContent());
        } while (page.hasNext());

        return allRentals;
    }

    @Override
    public Pair<LocalDateTime, LocalDateTime> getDateRange(LocalDate startDate, LocalDate endDate, ReportingConstants.TimePeriod defaultPeriod) {
        LocalDate start = startDate;
        LocalDate end = endDate;

        if (start == null) {
            end = LocalDate.now();
            start = end.minus(defaultPeriod.getValue(), defaultPeriod.getUnit());
        }
        if (end == null) {
            end = LocalDate.now();
        }

        return Pair.of(
                start.atStartOfDay(),
                end.plusDays(1).atStartOfDay()
        );
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
        return excelReportGenericTable.generateGenericTableExcel(headers, data);
    }
}