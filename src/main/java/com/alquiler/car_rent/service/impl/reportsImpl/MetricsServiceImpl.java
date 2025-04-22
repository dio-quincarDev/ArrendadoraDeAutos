package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private LocalDateTime toDateTime(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    private List<Rental> getRentalsInRange(LocalDateTime start, LocalDateTime end) {
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
    public long getTotalRentals(LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, ReportingConstants.TimePeriod.MONTHLY);
        return rentalRepository.countInDateRange(dateRange.getFirst(), dateRange.getSecond());
    }

    @Override
    public double getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, ReportingConstants.TimePeriod.MONTHLY);
        List<Rental> rentals = getRentalsInRange(dateRange.getFirst(), dateRange.getSecond());
        return rentals.stream()
                .mapToDouble(rental -> rental.getTotalPrice().doubleValue())
                .sum();
    }

    @Override
    public long getUniqueVehiclesRented(LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, ReportingConstants.TimePeriod.MONTHLY);
        List<Rental> rentals = getRentalsInRange(dateRange.getFirst(), dateRange.getSecond());
        return rentals.stream()
                .map(Rental::getVehicle)
                .distinct()
                .count();
    }

    @Override
    public Map<String, Object> getMostRentedVehicle(LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, ReportingConstants.TimePeriod.MONTHLY);
        List<Rental> rentals = getRentalsInRange(dateRange.getFirst(), dateRange.getSecond());
        return rentals.stream()
                .collect(Collectors.groupingBy(Rental::getVehicle, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("brand", ((Vehicle) entry.getKey()).getBrand());
                    result.put("model", ((Vehicle) entry.getKey()).getModel());
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
    public List<Map<String, Object>> getRentalTrends(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, period);
        LocalDate start = dateRange.getFirst().toLocalDate();
        LocalDate end = dateRange.getSecond().toLocalDate().minusDays(1);

        DateTimeFormatter formatter;

        switch (period) {
            case ReportingConstants.TimePeriod.MONTHLY:
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                break;
            case ReportingConstants.TimePeriod.QUARTERLY:
                formatter = DateTimeFormatter.ofPattern("yyyy-QQQ");
                break;
            case ReportingConstants.TimePeriod.BIANNUAL:
                formatter = DateTimeFormatter.ofPattern("yyyy-'H'");
                break;
            case ReportingConstants.TimePeriod.ANNUAL:
                formatter = DateTimeFormatter.ofPattern("yyyy");
                break;
            default:
                formatter = DateTimeFormatter.ISO_DATE;
                break;
        }

        LocalDate current = start;
        List<Map<String, Object>> trends = new ArrayList<>();

        while (!current.isAfter(end)) {
            LocalDate next = current.plus(period.getValue(), period.getUnit());
            long rentalCount = rentalRepository.countInDateRange(
                    toDateTime(current),
                    toDateTime(next)
            );
            trends.add(Map.of("period", current.format(formatter), "rentalCount", rentalCount));
            current = next;
        }

        return trends;
    }

}