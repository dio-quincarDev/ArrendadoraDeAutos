package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Customer;
import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.repositories.CustomerRepository;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.service.reportService.MetricsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MetricsServiceImpl implements MetricsService {

    private final RentalRepository rentalRepository;
    private final CustomerRepository customerRepository;

    @Value("${reporting.page.size:100}")
    private int pageSize;

    public MetricsServiceImpl(RentalRepository rentalRepository, CustomerRepository customerRepository) {
        this.rentalRepository = rentalRepository;
        this.customerRepository = customerRepository;
    }

    private Pair<LocalDateTime, LocalDateTime> getDateRange(LocalDate startDate, LocalDate endDate,
                                                            ReportingConstants.TimePeriod defaultPeriod) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().minus(defaultPeriod.getValue(), defaultPeriod.getUnit());
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return Pair.of(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }

    private List<Rental> getRentalsInRange(LocalDateTime start, LocalDateTime end) {
        int pageNum = 0;
        List<Rental> allRentals = new ArrayList<>();
        Page<Rental> page;
        do {
            page = rentalRepository.searchByDateRange(start, end, PageRequest.of(pageNum++, pageSize));
            allRentals.addAll(page.getContent());
        } while (page.hasNext());
        return allRentals;
    }

    @Override
    public long getTotalRentals(LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, ReportingConstants.TimePeriod.MONTHLY);
        return rentalRepository.countByDateRange(dateRange.getFirst(), dateRange.getSecond());
    }

    @Override
    public double getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, ReportingConstants.TimePeriod.MONTHLY);
        return rentalRepository.getTotalRevenueInRange(dateRange.getFirst(), dateRange.getSecond());
    }

    @Override
    public long getUniqueVehiclesRented(LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, ReportingConstants.TimePeriod.MONTHLY);
        return getRentalsInRange(dateRange.getFirst(), dateRange.getSecond()).stream()
                .map(Rental::getVehicle)
                .distinct()
                .count();
    }

    @Override
    public Map<String, Object> getMostRentedVehicle(LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, ReportingConstants.TimePeriod.MONTHLY);
        return getRentalsInRange(dateRange.getFirst(), dateRange.getSecond()).stream()
                .collect(Collectors.groupingBy(Rental::getVehicle, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    Vehicle vehicle = entry.getKey();
                    result.put("brand", vehicle.getBrand());
                    result.put("model", vehicle.getModel());
                    result.put("rentalCount", entry.getValue());
                    return result;
                })
                .orElse(new HashMap<>());
    }

    @Override
    public long getNewCustomersCount(LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, ReportingConstants.TimePeriod.MONTHLY);
        return customerRepository.countByCreatedAtBetween(dateRange.getFirst(), dateRange.getSecond());
    }

    @Override
    public List<Map<String, Object>> getRentalTrends(ReportingConstants.TimePeriod period,
                                                     LocalDate startDate,
                                                     LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, period);
        List<Map<String, Object>> trendsData = rentalRepository.findRentalTrends(
                dateRange.getFirst(),
                dateRange.getSecond()
        );
        DateTimeFormatter formatter = getFormatterForPeriod(period);
        return trendsData.stream()
                .map(entry -> {
                    String rawPeriod = (String) entry.get("period");
                    LocalDate date = LocalDate.parse(rawPeriod + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    return Map.of(
                            "period", date.format(formatter),
                            "rentalCount", entry.get("rentalCount")
                    );
                })
                .collect(Collectors.toList());
    }

    private DateTimeFormatter getFormatterForPeriod(ReportingConstants.TimePeriod period) {
        switch (period) {
            case MONTHLY: return DateTimeFormatter.ofPattern("yyyy-MM");
            case QUARTERLY: return DateTimeFormatter.ofPattern("yyyy-'Q'Q");
            case BIANNUAL: return DateTimeFormatter.ofPattern("yyyy-'H'");
            case ANNUAL: return DateTimeFormatter.ofPattern("yyyy");
            default: return DateTimeFormatter.ISO_DATE;
        }
    }

    @Override
    public long getUniqueCustomersRented(LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, ReportingConstants.TimePeriod.MONTHLY);
        return getRentalsInRange(dateRange.getFirst(), dateRange.getSecond()).stream()
                .map(Rental::getCustomer)
                .distinct()
                .count();
    }

    @Override
    public double getAverageRentalDuration(LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, ReportingConstants.TimePeriod.MONTHLY);
        List<Rental> rentals = getRentalsInRange(dateRange.getFirst(), dateRange.getSecond());
        if (rentals.isEmpty()) return 0;
        long total = rentals.stream()
                .mapToLong(r -> ChronoUnit.DAYS.between(r.getStartDate(), r.getEndDate()))
                .sum();
        return (double) total / rentals.size();
    }

    @Override
    public long getActiveCustomersCount(LocalDate startDate, LocalDate endDate) {
        return getUniqueCustomersRented(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getTopCustomersByRentals(LocalDate startDate, LocalDate endDate, int limit) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, ReportingConstants.TimePeriod.MONTHLY);
        return getRentalsInRange(dateRange.getFirst(), dateRange.getSecond()).stream()
                .collect(Collectors.groupingBy(Rental::getCustomer, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Customer, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> customerMap = new HashMap<>();
                    customerMap.put("customerId", entry.getKey().getId());
                    customerMap.put("name", entry.getKey().getName());
                    customerMap.put("email", entry.getKey().getEmail());
                    customerMap.put("rentalCount", entry.getValue());
                    return customerMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Double> getAverageRentalDurationByCustomer(LocalDate startDate, LocalDate endDate, List<Long> customerIds) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, ReportingConstants.TimePeriod.MONTHLY);
        return rentalRepository.findAverageDurationByCustomer(
                        dateRange.getFirst(),
                        dateRange.getSecond(),
                        customerIds
                ).stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Double) arr[1]
                ));
    }

    @Override
    public Map<Vehicle, Long> getVehicleUsage(LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, ReportingConstants.TimePeriod.MONTHLY);
        return rentalRepository.findVehicleUsage(
                        dateRange.getFirst(),
                        dateRange.getSecond()
                ).stream()
                .collect(Collectors.toMap(
                        arr -> (Vehicle) arr[0],
                        arr -> (Long) arr[1]
                ));
    }
}
