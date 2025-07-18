package com.alquiler.car_rent.commons.constants;

import java.time.temporal.ChronoUnit;

public class ReportingConstants {
    public static final int PAGE_SIZE = 100;

    /**
     * Períodos de tiempo para reportes y métricas.
     * <strong>Límite máximo:</strong> 12 meses.
     */
    public enum TimePeriod {
        MONTHLY(1, ChronoUnit.MONTHS),
        QUARTERLY(3, ChronoUnit.MONTHS),
        BIANNUAL(6, ChronoUnit.MONTHS),
        ANNUAL(12, ChronoUnit.MONTHS),
        ALL_TIME(0,null);

        public static final int MAX_PERIOD_VALUE = 12;
        private final int value;
        private final ChronoUnit unit;

        TimePeriod(int value, ChronoUnit unit) {
            if (value > MAX_PERIOD_VALUE) {
                throw new IllegalArgumentException("El valor máximo permitido es " + MAX_PERIOD_VALUE);
            }
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
    public enum OutputFormat {
        PDF, EXCEL, HTML, JSON, CHART_PNG, CHART_SVG
    }

    /**
     * Tipos de reportes soportados
     */
    public enum ReportType {
        RENTAL_SUMMARY("Resumen de Alquileres"),
        VEHICLE_USAGE("Uso de Vehículos"),
        REVENUE_ANALYSIS("Análisis de Ingresos"),
        CUSTOMER_ACTIVITY("Actividad de Clientes"),
        MOST_RENTED_VEHICLES("Vehículos Más Alquilados"),
        RENTAL_TRENDS("Tendencias de Alquiler"),
        GENERIC_METRICS("Métricas Generales"),
        TOP_CUSTOMERS_BY_RENTALS("Cliente con mas alquileres por periodo"),
        AVERAGE_RENTAL_DURATION("Duracion Promedio de Alquiler"),
        RENTALS_BY_VEHICLE_TYPE("Alquileres por Tipo de Vehículo"),
        REVENUE_BY_VEHICLE_TYPE("Ingresos por Tipo de Vehículo"),
        RENTALS_BY_PRICING_TIER("Alquileres por Nivel de Precios"),
        REVENUE_BY_PRICING_TIER("Ingresos por Nivel de Precios");

        private final String title;

        ReportType(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }
}