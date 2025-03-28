package com.alquiler.car_rent.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Interfaz que define operaciones comunes para todos los tipos de reportes
 */
public interface ReportingService {
    
    /**
     * Genera datos para reportes con diferentes períodos de tiempo
     * 
     * @param period El período de tiempo (MONTHLY, QUARTERLY, BIANNUAL, ANNUAL)
     * @param startDate Fecha de inicio opcional para filtrar datos
     * @param endDate Fecha final opcional para filtrar datos
     * @return Mapa con datos preparados para reportes
     */
    Map<String, Object> generateReportData(TimePeriod period, LocalDate startDate, LocalDate endDate);
    
    /**
     * Genera un reporte en el formato solicitado
     * 
     * @param format El formato de salida (PDF, EXCEL, HTML, JSON, CHART)
     * @param reportType El tipo de reporte (RENTAL_SUMMARY, VEHICLE_USAGE, REVENUE, etc)
     * @param period El período de tiempo
     * @param startDate Fecha opcional de inicio
     * @param endDate Fecha opcional final
     * @return Los bytes del reporte generado
     */
    byte[] generateReport(OutputFormat format, ReportType reportType, TimePeriod period, 
                          LocalDate startDate, LocalDate endDate);
    
    /**
     * Períodos de tiempo para reportes
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
}