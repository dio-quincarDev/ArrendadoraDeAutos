package com.alquiler.car_rent.service.reportService;

import com.alquiler.car_rent.commons.constants.ReportingConstants.TimePeriod;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.commons.enums.PricingTier;
import com.alquiler.car_rent.commons.enums.VehicleType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MetricsService {
    /**
     * Obtiene el total de alquileres en un período
     */
    long getTotalRentals(TimePeriod period, LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene el total de ingresos en un período
     */
    double getTotalRevenue(TimePeriod period, LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene el número de vehículos únicos alquilados
     */
    long getUniqueVehiclesRented(TimePeriod period, LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene información del vehículo más alquilado
     */
    Map<String, Object> getMostRentedVehicle(TimePeriod period, LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene el número de nuevos clientes en un período
     */
    long getNewCustomersCount(TimePeriod period,LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene las tendencias de alquileres por período
     */
    List<Map<String, Object>> getRentalTrends(TimePeriod period, LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene el número de clientes únicos que realizaron al menos un alquiler en el período
     */
    long getUniqueCustomersRented(TimePeriod period, LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene la duración promedio de los alquileres en días
     */
    double getAverageRentalDuration(TimePeriod period, LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene el número de clientes activos (con al menos un alquiler) en el período
     */
    long getActiveCustomersCount(TimePeriod period,LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene una lista de los 'limit' clientes que realizaron más alquileres
     */
    List<Map<String, Object>> getTopCustomersByRentals(TimePeriod period,LocalDate startDate, LocalDate endDate, int limit);

    /**
     * Obtiene la duración promedio de los alquileres para una lista específica de IDs de clientes
     */
    Map<String, Double> getAverageRentalDurationByCustomer(TimePeriod period, LocalDate startDate, LocalDate endDate, List<Long> customerIds);


    Map<Vehicle, Long> getVehicleUsage(TimePeriod period, LocalDate startDate, LocalDate endDate);
    
    long getAvailableVehiclesCount();


    
	List<Map<String, Object>> getCustomerActivity(TimePeriod period, LocalDate startDate, LocalDate endDate);

    Map<VehicleType, Long> getRentalsCountByVehicleType(TimePeriod period, LocalDate startDate, LocalDate endDate);
    Map<VehicleType, Double> getRevenueByVehicleType(TimePeriod period, LocalDate startDate, LocalDate endDate);
    Map<PricingTier, Long> getRentalsCountByPricingTier(TimePeriod period, LocalDate startDate, LocalDate endDate);
    Map<PricingTier, Double> getRevenueByPricingTier(TimePeriod period, LocalDate startDate, LocalDate endDate);
}