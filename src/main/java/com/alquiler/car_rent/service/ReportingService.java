package com.alquiler.car_rent.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Interfaz que define operaciones comunes para todos los tipos de reportes y métricas del dashboard
 */
public interface ReportingService {

    /**
     * Genera datos para reportes con diferentes períodos de tiempo
     *
     * @param period    El período de tiempo (MONTHLY, QUARTERLY, BIANNUAL, ANNUAL)
     * @param startDate Fecha de inicio opcional para filtrar datos
     * @param endDate   Fecha final opcional para filtrar datos
     * @return Mapa con datos preparados para reportes
     */
    Map<String, Object> generateReportData(TimePeriod period, LocalDate startDate, LocalDate endDate);

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
    byte[] generateReport(OutputFormat format, ReportType reportType, TimePeriod period,
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
    List<Map<String, Object>> getRentalTrends(TimePeriod period, LocalDate startDate, LocalDate endDate);


    /**
     * Períodos de tiempo para reportes y métricas
     */
    enum TimePeriod {
        MONTHLY(1, ChronoUnit.MONTHS),
        QUARTERLY(3, ChronoUnit.MONTHS),
        BIANNUAL(6, ChronoUnit.MONTHS),
        ANNUAL(12, ChronoUnit.MONTHS);

        private final int value;
        private final ChronoUnit unit;

        TimePeriod(int value, ChronoUnit unit) {
            this.value = value;
            this.unit = unit;
        }

        public int getValue() {
            return value;
        }

        public ChronoUnit getUnit() {
            return unit;
        }
    }

    /**
     * Formatos de salida soportados
     */
    enum OutputFormat {
        PDF, EXCEL, HTML, JSON, CHART_PNG, CHART_SVG
    }

    /**
     * Tipos de reportes soportados
     */
    enum ReportType {
        RENTAL_SUMMARY,
        VEHICLE_USAGE,
        REVENUE_ANALYSIS,
        CUSTOMER_ACTIVITY,
        MOST_RENTED_CARS
    }

    /**
     * Genera un archivo Excel genérico a partir de una tabla de datos enviada desde frontend.
     *
     * @param headers Encabezados de las columnas
     * @param data Filas de datos (cada fila es una lista de strings)
     * @return Excel en bytes listo para descargar
     */
    byte[] generateGenericTableExcel(List<String> headers, List<List<String>> data);


}