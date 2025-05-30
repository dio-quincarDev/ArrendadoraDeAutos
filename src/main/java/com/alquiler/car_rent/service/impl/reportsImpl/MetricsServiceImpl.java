package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.commons.enums.VehicleStatus;
import com.alquiler.car_rent.repositories.CustomerRepository;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.repositories.VehicleRepository;
import com.alquiler.car_rent.service.reportService.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final VehicleRepository vehicleRepository;

    private static final LocalDateTime SAFE_MIN_DATE = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime SAFE_MAX_DATE = LocalDateTime.of(2150, 1, 1, 0, 0);

    @Value("${reporting.page.size:100}")
    private int pageSize;

    public MetricsServiceImpl(RentalRepository rentalRepository, CustomerRepository customerRepository,
    		VehicleRepository vehicleRepository) {
        this.rentalRepository = rentalRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
    }
    private Pair<LocalDateTime, LocalDateTime> getDateRange(LocalDate startDate, LocalDate endDate,
                                                            ReportingConstants.TimePeriod period) {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        if (period == null || period.name().equals("ALL_TIME")) {
            logger.info("Calculando rango de fechas seguro para 'ALL_TIME'.");
            startDateTime = SAFE_MIN_DATE;
            endDateTime = SAFE_MAX_DATE;
        } else {
            LocalDate start;
            LocalDate end;
            if (startDate != null) {
                start = startDate;
                logger.info("Usando startDate proporcionado: {}", start);
            } else {
                start = LocalDate.now().minus(period.getValue(), period.getUnit());
                logger.info("Usando startDate por Defecto ({}) : {}", period, start);
            }
            if (endDate != null) {
                end = endDate;
                logger.info("Usando endDate proporcionado: {}", end);
            } else {
                end = LocalDate.now();
                logger.info("Usando endDate por Defecto ({}) : {}", period, end);
            }

            startDateTime = start.atStartOfDay();
            endDateTime = end.plusDays(1).atStartOfDay();
        }

        logger.info("Rango de Fechas/hora calculado - Start: {}, End: {}", startDateTime, endDateTime);
        return Pair.of(startDateTime, endDateTime);
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
    public long getTotalRentals(ReportingConstants.TimePeriod period,LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, period);
        Number countResult = rentalRepository.countByDateRange(dateRange.getFirst(), dateRange.getSecond());
        return countResult.longValue();
    }

    @Override
    public double getTotalRevenue(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, period);
        return rentalRepository.getTotalRevenueInRange(dateRange.getFirst(), dateRange.getSecond());
    }

    @Override
    public long getUniqueVehiclesRented(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, period);
        return getRentalsInRange(dateRange.getFirst(), dateRange.getSecond()).stream()
                .map(Rental::getVehicle)
                .distinct()
                .count();
    }

    @Override
    public Map<String, Object> getMostRentedVehicle(ReportingConstants.TimePeriod period,LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, period);

        // Usar la consulta optimizada del repositorio en lugar de la lógica manual
        List<Map<String,Object>> results = rentalRepository.findMostRentedVehicle(dateRange.getFirst(), dateRange.getSecond());

        if (results == null || results.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> mostRented = results.get(0);
        return Map.of(
                "brand", mostRented.get("brand"),
                "model", mostRented.get("model"),
                "rentalCount", ((Number) mostRented.get("rentalCount")).longValue()
        );
    }
    
    @Override
    public Map<Vehicle, Long> getVehicleUsage(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, period);

        return rentalRepository.findVehicleUsage(
                        dateRange.getFirst(),
                        dateRange.getSecond()
                ).stream()
                .collect(Collectors.toMap(
                        entry -> {
                            // Construye un Vehicle temporal solo con los datos necesarios
                            Vehicle vehicle = new Vehicle();
                            vehicle.setId(((Number) entry.get("vehicleId")).longValue());
                            vehicle.setBrand((String) entry.get("brand"));
                            vehicle.setModel((String) entry.get("model"));
                            return vehicle;
                        },
                        entry -> (Long) entry.get("usageCount")
                ));
    }

    @Override
    public long getNewCustomersCount(ReportingConstants.TimePeriod period,  LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, period);
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
                    Map<String,Object> result = new LinkedHashMap<>();
                    result.put("period", date.format(formatter));
                    result.put("rentalCount", entry.get("rentalCount"));

                            if (entry.containsKey("totalRevenue")){
                                result.put("totalRevenue", entry.get("totalRevenue"));
                    }
                            return result;
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
    public long getUniqueCustomersRented(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, period);
        return getRentalsInRange(dateRange.getFirst(), dateRange.getSecond()).stream()
                .map(Rental::getCustomer)
                .distinct()
                .count();
    }

    @Override
    public double getAverageRentalDuration(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, period);
        List<Rental> rentals = getRentalsInRange(dateRange.getFirst(), dateRange.getSecond());
        if (rentals.isEmpty()) return 0;
        long total = rentals.stream()
                .mapToLong(r -> ChronoUnit.DAYS.between(r.getStartDate(), r.getEndDate()))
                .sum();
        return (double) total / rentals.size();
    }

    @Override
    public long getActiveCustomersCount(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate) {
        return getUniqueCustomersRented(period, endDate, startDate);
    }

    @Override
    public List<Map<String, Object>> getTopCustomersByRentals(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate, int limit) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, period);

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

                    Long customerId = ((Number) entry[0]).longValue();
                    String name = (String) entry[1];
                    Long rentalCount = ((Number) entry[2]).longValue();

                    Map<String, Object> customerMap = new HashMap<>();
                    customerMap.put("customerId", customerId);
                    customerMap.put("name", name);
                    customerMap.put("rentalCount", rentalCount);
                    return customerMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Double> getAverageRentalDurationByCustomer(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate, List<Long> customerIds) {
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, period);
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
    public List<Map<String, Object>>getCustomerActivity(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate){
      LocalDateTime start;
      LocalDateTime end;
      
      if(period == ReportingConstants.TimePeriod.ALL_TIME) {
    	  start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.of(1900, 1, 1, 0, 0);
    	  end = endDate != null ? endDate.plusDays(1).atStartOfDay() : LocalDateTime.of(2150, 1, 1, 0, 0);

    	} else {
    		start = startDate != null ? startDate.atStartOfDay() : LocalDate.now().minus(period.getValue(), period.getUnit()).atStartOfDay();
    		end = endDate != null ? endDate.plusDays(1).atStartOfDay() : LocalDate.now().plusDays(1).atStartOfDay();	
    	}
      List<Object[]> results = rentalRepository.findTopCustomersByRentals(start, end, Pageable.unpaged());
      List<Map<String, Object>> customerActivityList = new ArrayList<>();
      
      for (Object[] result : results) {
    	  Map<String, Object> customerData = new HashMap<>();
    	  customerData.put("name", result[1]);
    	  customerData.put("rentals", result[2]);
    	  customerData.put("revenue", result[3]);
    	  
    	  customerActivityList.add(customerData);
      }
      
      return customerActivityList;
      
    }
	@Override
	public long getAvailableVehiclesCount() {
		return vehicleRepository.findByStatus(VehicleStatus.AVAILABLE).size();
	
	}
   
}