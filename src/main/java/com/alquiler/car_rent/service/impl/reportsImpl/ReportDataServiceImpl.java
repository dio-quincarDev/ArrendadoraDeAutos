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

        // Usamos el método compartido getDateRange de la interfaz
        Pair<LocalDateTime, LocalDateTime> dateRange = getDateRange(startDate, endDate, timePeriod);
        LocalDate start = dateRange.getFirst().toLocalDate();
        LocalDate end = dateRange.getSecond().toLocalDate().minusDays(1);

        logger.info("Rango de fechas calculado - Start: {}, End: {}", start, end);

        List<Rental> rentals = getRentalsInRange(dateRange.getFirst(), dateRange.getSecond());
        logger.info("Total de alquileres obtenidos en el rango: {}", rentals.size());

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("period", timePeriod);
        reportData.put("startDate", startDate != null ? startDate : start);
        reportData.put("endDate", endDate != null ? endDate : end);

        addBasicMetrics(reportData, rentals);
        addCustomerMetrics(reportData, start, end);
        addVehicleMetrics(reportData, start, end);
        addTrendsAndAdvancedMetrics(reportData, timePeriod, start, end);

        logger.info("Datos del reporte generados: {}", reportData);
        return reportData;
    }

    private void addBasicMetrics(Map<String, Object> reportData, List<Rental> rentals) {
        long totalRentals = rentals.size();
        double totalRevenue = calculateTotalRevenue(rentals);
        reportData.put("totalRentals", totalRentals);
        reportData.put("totalRevenue", totalRevenue);
        logger.info("Métricas básicas añadidas - Total Rentals: {}, Total Revenue: {}", totalRentals, totalRevenue);
    }

    private double calculateTotalRevenue(List<Rental> rentals) {
        double revenue = rentals.stream()
                .mapToDouble(rental -> rental.getTotalPrice().doubleValue())
                .sum();
        logger.info("Ingresos totales calculados: {}", revenue);
        return revenue;
    }

    private void addCustomerMetrics(Map<String, Object> reportData, LocalDate start, LocalDate end) {
        Long uniqueCustomers = metricsService.getUniqueCustomersRented(start, end);
        Long activeCustomers = metricsService.getActiveCustomersCount(start, end);
        Long newCustomers = metricsService.getNewCustomersCount(start, end);
        reportData.put("uniqueCustomers", uniqueCustomers);
        reportData.put("activeCustomers", activeCustomers);
        reportData.put("newCustomers", newCustomers);
        logger.info("Métricas de clientes añadidas - Unique Customers: {}, Active Customers: {}, New Customers: {}",
                uniqueCustomers, activeCustomers, newCustomers);

        List<Map<String, Object>> topCustomers = metricsService.getTopCustomersByRentals(start, end, DEFAULT_TOP_CUSTOMERS_LIMIT);
        reportData.put("topCustomersByRentals", topCustomers);
        logger.info("Top Customers por Alquileres obtenidos: {}", topCustomers);

        if (!topCustomers.isEmpty()) {
            List<Long> customerIds = extractCustomerIds(topCustomers);
            Map<String, Double> averageRentalDurationByTopCustomers = metricsService.getAverageRentalDurationByCustomer(start, end, customerIds);
            reportData.put("averageRentalDurationByTopCustomers", averageRentalDurationByTopCustomers);
            logger.info("Duración promedio de alquileres por Top Customers: {}", averageRentalDurationByTopCustomers);
        } else {
            logger.info("No hay Top Customers para calcular la duración promedio de alquileres.");
        }
    }

    private List<Long> extractCustomerIds(List<Map<String, Object>> customers) {
        List<Long> customerIds = customers.stream()
                .map(c -> (Long) c.get("customerId"))
                .collect(Collectors.toList());
        logger.info("IDs de clientes extraídos: {}", customerIds);
        return customerIds;
    }

    private void addVehicleMetrics(Map<String, Object> reportData, LocalDate start, LocalDate end) {
        Map<Vehicle, Long> vehicleUsage = metricsService.getVehicleUsage(start, end);
        reportData.put("vehicleUsage", vehicleUsage);
        logger.info("Uso de vehículos obtenido: {}", vehicleUsage);

        Map<String, Object> mostRentedVehicle = metricsService.getMostRentedVehicle(start, end);
        reportData.put("mostRentedVehicle", mostRentedVehicle);
        logger.info("Vehículo más alquilado obtenido: {}", mostRentedVehicle);
    }

    private void addTrendsAndAdvancedMetrics(Map<String, Object> reportData,
                                             ReportingConstants.TimePeriod period,
                                             LocalDate start,
                                             LocalDate end) {
        List<Map<String, Object>> rentalTrends = metricsService.getRentalTrends(period, start, end);
        reportData.put("rentalTrends", rentalTrends);
        logger.info("Tendencias de alquileres obtenidas: {}", rentalTrends);

        Double averageRentalDuration = metricsService.getAverageRentalDuration(start, end);
        reportData.put("averageRentalDuration", averageRentalDuration);
        logger.info("Duración promedio de alquileres obtenida: {}", averageRentalDuration);
    }

    @Override
    public List<Rental> getRentalsInRange(LocalDateTime start, LocalDateTime end) {
        List<Rental> allRentals = new ArrayList<>();
        int pageNum = 0;
        Page<Rental> page;

        logger.info("Obteniendo alquileres en el rango - Start: {}, End: {}", start, end);
        do {
            page = rentalRepository.searchByDateRange(start, end, PageRequest.of(pageNum++, pageSize));
            logger.info("Página {} de alquileres obtenida - Tamaño: {}, Total elementos: {}",
                    pageNum, page.getContent().size(), page.getTotalElements());
            allRentals.addAll(page.getContent());
        } while (page.hasNext());

        logger.info("Total de alquileres recuperados: {}", allRentals.size());
        return allRentals;
    }

    @Override
    public Pair<LocalDateTime, LocalDateTime> getDateRange(LocalDate startDate,
                                                           LocalDate endDate,
                                                           ReportingConstants.TimePeriod defaultPeriod) {
        LocalDate start;
        LocalDate end;
        if (startDate != null) {
            start = startDate;
            logger.info("Usando startDate proporcionado: {}", start);
        } else {
            start = LocalDate.now().minus(defaultPeriod.getValue(), defaultPeriod.getUnit());
            logger.info("Usando startDate por defecto ({}): {}", defaultPeriod, start);
        }

        if (endDate != null) {
            end = endDate;
            logger.info("Usando endDate proporcionado: {}", end);
        } else {
            end = LocalDate.now();
            logger.info("Usando endDate por defecto: {}", end);
        }
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay();
        logger.info("Rango de fechas/hora calculado - Start: {}, End: {}", startDateTime, endDateTime);
        return Pair.of(startDateTime, endDateTime);
    }

    @Override
    public LocalDateTime toDateTime(LocalDate date) {
        LocalDateTime dateTime = date != null ? date.atStartOfDay() : null;
        logger.info("Convertido LocalDate {} a LocalDateTime: {}", date, dateTime);
        return dateTime;
    }

    @Override
    public String formatDate(LocalDate date) {
        String formattedDate = date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
        logger.info("Formateado LocalDate {} a String: {}", date, formattedDate);
        return formattedDate;
    }

    @Override
    public byte[] generateGenericTableExcel(List<String> headers, List<List<String>> data) {
        logger.info("Generando tabla genérica de Excel con headers: {} y datos: {}", headers, data);
        byte[] excelBytes = excelReportService.generateGenericTableExcel(headers, data);
        logger.info("Tabla genérica de Excel generada. Tamaño: {} bytes", excelBytes.length);
        return excelBytes;
    }
}