package com.alquiler.car_rent.service.reportService;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.commons.enums.VehicleType;
import com.alquiler.car_rent.commons.enums.PricingTier;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportingService {


    /**
     * Genera datos para reportes con diferentes períodos de tiempo
     *
     * @param period    El período de tiempo (MONTHLY, QUARTERLY, BIANNUAL, ANNUAL)
     * @param startDate Fecha de inicio opcional para filtrar datos
     * @param endDate   Fecha final opcional para filtrar datos
     * @return Mapa con datos preparados para reportes
     */
    Map<String, Object> generateReportData(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate);

    /**
     * Genera un reporte en el formato solicitado
     *
     * @param format     El formato de salida (PDF, EXCEL, HTML, JSON, CHART)
     * @param reportType El tipo de reporte (RENTAL_SUMMARY, VEHICLE_USAGE, REVENUE, etc)
     * @param period     El período de tiempo
     * @param startDate  Fecha opcional de inicio
     * @param endDate    Fecha opcional final
     * @return Los bytes del reporte generado
     */
    byte[] generateReport(ReportingConstants.OutputFormat format, ReportingConstants.ReportType reportType, ReportingConstants.TimePeriod period,
                          LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene el total de alquileres para el período especificado.
     *
     * @param startDate Fecha de inicio opcional
     * @param endDate   Fecha final opcional
     * @return El total de alquileres.
     */
    long getTotalRentals(LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene los ingresos totales para el período especificado.
     *
     * @param startDate Fecha de inicio opcional
     * @param endDate   Fecha final opcional
     * @return Los ingresos totales.
     */
    double getTotalRevenue(LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene el número de vehículos únicos alquilados en el período especificado.
     *
     * @param startDate Fecha de inicio opcional
     * @param endDate   Fecha final opcional
     * @return El número de vehículos únicos alquilados.
     */
    long getUniqueVehiclesRented(LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene el vehículo más alquilado en el período especificado.
     *
     * @param startDate Fecha de inicio opcional
     * @param endDate   Fecha final opcional
     * @return Un mapa con la marca y modelo del vehículo más alquilado y la cantidad de alquileres.
     */
    Map<String, Object> getMostRentedVehicle(LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene el número de nuevos clientes en el período especificado.
     *
     * @param startDate Fecha de inicio opcional
     * @param endDate   Fecha final opcional
     * @return El número de nuevos clientes.
     */
    long getNewCustomersCount(LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene las tendencias de alquileres por período (ej. mensual).
     *
     * @param startDate Fecha de inicio opcional
     * @param endDate   Fecha final opcional
     * @return Una lista de mapas con el período y la cantidad de alquileres.
     */
    List<Map<String, Object>> getRentalTrends(ReportingConstants.TimePeriod period, LocalDate startDate, LocalDate endDate);

    Map<Vehicle, Long> getVehicleUsage(LocalDate startDate, LocalDate endDate);

    /**
     * Genera un archivo Excel genérico a partir de una tabla de datos enviada desde frontend.
     *
     * @param headers Encabezados de las columnas
     * @param data    Filas de datos (cada fila es una lista de strings)
     * @return Excel en bytes listo para descargar
     */
    byte[] generateGenericTableExcel(List<String> headers, List<List<String>> data);

    Map<VehicleType, Long> getRentalsByVehicleType(LocalDate startDate, LocalDate endDate);

    Map<VehicleType, Double> getRevenueByVehicleType(LocalDate startDate, LocalDate endDate);

    Map<PricingTier, Long> getRentalsByPricingTier(LocalDate startDate, LocalDate endDate);

    Map<PricingTier, Double> getRevenueByPricingTier(LocalDate startDate, LocalDate endDate);
}