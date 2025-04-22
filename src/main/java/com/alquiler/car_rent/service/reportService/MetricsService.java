package com.alquiler.car_rent.service.reportService;

import com.alquiler.car_rent.commons.constants.ReportingConstants.TimePeriod;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MetricsService {
    /**
     * Obtiene el total de alquileres en un período
     */
    long getTotalRentals(LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene el total de ingresos en un período
     */
    double getTotalRevenue(LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene el número de vehículos únicos alquilados
     */
    long getUniqueVehiclesRented(LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene información del vehículo más alquilado
     */
    Map<String, Object> getMostRentedVehicle(LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene el número de nuevos clientes en un período
     */
    long getNewCustomersCount(LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene las tendencias de alquileres por período
     */
    List<Map<String, Object>> getRentalTrends(TimePeriod period, LocalDate startDate, LocalDate endDate);
}
