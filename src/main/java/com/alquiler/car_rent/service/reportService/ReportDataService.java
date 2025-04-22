package com.alquiler.car_rent.service.reportService;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Rental;
import org.springframework.data.util.Pair;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReportDataService {

    Map<String, Object> generateReportData(ReportingConstants.TimePeriod timePeriod, LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene los alquileres en un rango de fechas
     */
    List<Rental> getRentalsInRange(LocalDateTime start, LocalDateTime end);
    /**
     * Calcula el rango de fechas según los parámetros
     */
    Pair<LocalDateTime, LocalDateTime> getDateRange(LocalDate startDate, LocalDate endDate, ReportingConstants.TimePeriod defaultPeriod);


    LocalDateTime toDateTime(LocalDate date);

    /**
     * Formatea una fecha para su visualización
     */
    String formatDate(LocalDate date);
}

