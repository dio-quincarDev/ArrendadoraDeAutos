package com.alquiler.car_rent.commons.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.alquiler.car_rent.commons.enums.PricingTier;
import com.alquiler.car_rent.commons.enums.VehicleStatus;
import com.alquiler.car_rent.commons.enums.VehicleType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO para la transferencia de datos de vehículos")
public class VehicleDto {
    @Schema(description = "ID único del vehículo", example = "1")
    private Long id;
    @Schema(description = "Marca del vehículo", example = "Toyota")
    private String brand;
    @Schema(description = "Modelo del vehículo", example = "Corolla")
    private String model;
    @Schema(description = "Año de fabricación del vehículo", example = "2020")
    private Integer year;
    @Schema(description = "Número de placa del vehículo", example = "ABC-123")
    private String plate;
    @Schema(description = "Estado actual del vehículo (ej. AVAILABLE, RENTED, MAINTENANCE)", example = "AVAILABLE")
    private VehicleStatus status;
    @Schema(description = "Tipo de vehículo (ej. SEDAN, SUV, TRUCK)", example = "SEDAN")
    private VehicleType vehicleType;
    @Schema(description = "Nivel de precios del vehículo (ej. STANDARD, PREMIUM)", example = "STANDARD")
    private PricingTier pricingTier;
    @Schema(description = "Tarifa diaria actual del vehículo", example = "50.00")
    private BigDecimal actualDailyRate;
    @Schema(description = "Fecha y hora de creación del registro del vehículo", example = "2023-07-21T10:00:00")
    private LocalDateTime createdAt;
}