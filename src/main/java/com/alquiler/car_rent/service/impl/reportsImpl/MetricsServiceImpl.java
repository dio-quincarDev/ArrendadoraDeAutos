package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Customer;
import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.repositories.CustomerRepository;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.service.reportService.MetricsService;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MetricsServiceImpl implements MetricsService {
    private static final Logger logger = LoggerFactory.getLogger(MetricsServiceImpl.class);

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

        // Usar la consulta optimizada del repositorio en lugar de la lógica manual
        List<Map<String,Object>> results = rentalRepository.findMostRentedVehicle(dateRange.getFirst(), dateRange.getSecond());

        if (results == null || results.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> mostRented = results.get(0);
        return Map.of(
                "brand", mostRented.get("brand"),
                "model", mostRented.get("model"),
                "rentalCount", mostRented.get("rentalCount")
        );
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

        // Usar la consulta optimizada del repositorio con paginación
        List<Object[]> topCustomers = rentalRepository.findTopCustomersByRentals(
                dateRange.getFirst(),
                dateRange.getSecond(),
                PageRequest.of(0, limit)
        );

        return topCustomers.stream()
                .map(entry -> {
                    logger.debug("Raw entry from findTopCustomersByRentals: {}", Arrays.toString(entry));
                    Object firstElement = entry[0];
                    Object secondElement = entry[1];
                    Object thirdElement = entry[2];

                    logger.debug("Types: entry[0]={}, entry[1]={}, entry[2]={}",
                            firstElement != null ? firstElement.getClass().getName() : null,
                            secondElement != null ? secondElement.getClass().getName() : null,
                            thirdElement != null ? thirdElement.getClass().getName() : null);


                    String name = (String) entry[1];
                    Long customerId = (Long) entry[0];
                    Long rentalCount = (Long) entry[2];

                    Map<String, Object> customerMap = new HashMap<>();
                    customerMap.put("customerId", customerId);
                    customerMap.put("name", name);
                    customerMap.put("rentalCount", rentalCount);
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
                        entry -> {
                            // Construye un Vehicle temporal solo con los datos necesarios
                            Vehicle vehicle = new Vehicle();
                            vehicle.setId((Long) entry.get("vehicleId"));
                            vehicle.setBrand((String) entry.get("brand"));
                            vehicle.setModel((String) entry.get("model"));
                            return vehicle;
                        },
                        entry -> (Long) entry.get("usageCount")
                ));
    }
}