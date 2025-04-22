package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.repositories.RentalRepository;
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

@Service
public class ReportDataServiceImpl implements ReportDataService {
    private final RentalRepository rentalRepository;

    @Value("${reporting.page.size:100}")
    private int pageSize;

    public ReportDataServiceImpl(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
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
        reportData.put("rentals", rentals); // Incluimos la lista de alquileres para otros servicios

        double totalRevenue = rentals.stream()
                .mapToDouble(rental -> rental.getTotalPrice().doubleValue())
                .sum();
        reportData.put("totalRevenue", totalRevenue);

        // Aquí puedes agregar más datos que necesites para diferentes tipos de reportes
        // Por ejemplo, agrupar por vehículo, cliente, etc.

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

}