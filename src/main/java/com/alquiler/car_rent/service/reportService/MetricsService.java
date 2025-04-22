package com.alquiler.car_rent.service.reportService;

import com.alquiler.car_rent.commons.constants.ReportingConstants.TimePeriod;
import com.alquiler.car_rent.commons.entities.Vehicle;

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

    /**
     * Obtiene el número de clientes únicos que realizaron al menos un alquiler en el período
     */
    long getUniqueCustomersRented(LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene la duración promedio de los alquileres en días
     */
    double getAverageRentalDuration(LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene el número de clientes activos (con al menos un alquiler) en el período
     */
    long getActiveCustomersCount(LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene una lista de los 'limit' clientes que realizaron más alquileres
     */
    List<Map<String, Object>> getTopCustomersByRentals(LocalDate startDate, LocalDate endDate, int limit);

    /**
     * Obtiene la duración promedio de los alquileres para una lista específica de IDs de clientes
     */
    Map<String, Double> getAverageRentalDurationByCustomer(LocalDate startDate, LocalDate endDate, List<Long> customerIds);


    Map<Vehicle, Long> getVehicleUsage(LocalDate startDate, LocalDate endDate);
}