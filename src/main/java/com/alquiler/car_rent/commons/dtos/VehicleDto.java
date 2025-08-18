package com.alquiler.car_rent.commons.dtos;

import com.alquiler.car_rent.commons.enums.PricingTier;
import com.alquiler.car_rent.commons.enums.VehicleStatus;
import com.alquiler.car_rent.commons.enums.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO para la transferencia de datos de vehículos")
public class VehicleDto {

    @Schema(description = "ID único del vehículo", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "La marca no puede estar vacía")
    @Size(max = 50, message = "La marca no puede tener más de 50 caracteres")
    @Schema(description = "Marca del vehículo", example = "Toyota")
    private String brand;

    @NotBlank(message = "El modelo no puede estar vacío")
    @Size(max = 50, message = "El modelo no puede tener más de 50 caracteres")
    @Schema(description = "Modelo del vehículo", example = "Corolla")
    private String model;

    @NotNull(message = "El año no puede ser nulo")
    @Min(value = 1980, message = "El año debe ser como mínimo 1980")
    @Schema(description = "Año de fabricación del vehículo", example = "2022")
    private Integer year;

    @NotBlank(message = "La placa no puede estar vacía")
    @Size(min = 6, max = 10, message = "La placa debe tener entre 6 y 10 caracteres")
    @Schema(description = "Número de placa del vehículo", example = "PA-1234")
    private String plate;

    @Schema(description = "Estado actual del vehículo (ej. AVAILABLE, RENTED, MAINTENANCE)", example = "AVAILABLE", accessMode = Schema.AccessMode.READ_ONLY)
    private VehicleStatus status;

    @NotNull(message = "El tipo de vehículo no puede ser nulo")
    @Schema(description = "Tipo de vehículo (ej. SEDAN, SUV, TRUCK)", example = "SEDAN")
    private VehicleType vehicleType;

    @NotNull(message = "El nivel de precios no puede ser nulo")
    @Schema(description = "Nivel de precios del vehículo (ej. STANDARD, PREMIUM)", example = "STANDARD")
    private PricingTier pricingTier;

    @Schema(description = "Tarifa diaria actual del vehículo", example = "55.50", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal actualDailyRate;

    @Schema(description = "Fecha y hora de creación del registro del vehículo", example = "2023-07-21T10:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}
