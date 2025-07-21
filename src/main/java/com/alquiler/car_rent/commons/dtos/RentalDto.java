package com.alquiler.car_rent.commons.dtos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import com.alquiler.car_rent.commons.enums.RentalStatus;
import com.alquiler.car_rent.commons.enums.PricingTier;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;


@Getter
@Schema(description = "DTO para la transferencia de datos de alquileres de vehículos")
public class RentalDto {

    // Getters y Setters
    @NotNull(message = "ID no puede ser nulo")
    @Schema(description = "ID único del alquiler", example = "1")
    private Long id;
	@NotNull(message = "Customer ID es requerido")
    @Schema(description = "ID del cliente que realiza el alquiler", example = "101")
    private Long customerId;
	@NotBlank(message = "Nombre de cliente es obligatorio")
    @Schema(description = "Nombre del cliente", example = "Juan Pérez")
    private String customerName;
    @NotNull(message = "Vehicle ID es requerido")
    @Schema(description = "ID del vehículo alquilado", example = "201")
    private Long vehicleId;
    
    @Schema(description = "Modelo del vehículo", example = "Corolla")
    private String vehicleModel;
    @Schema(description = "Marca del vehículo", example = "Toyota")
    private String vehicleBrand;
    @Schema(description = "Estado actual del alquiler", example = "RENTED")
    private RentalStatus rentalStatus;
    @Schema(description = "Tipo de vehículo", example = "SEDAN")
    private String vehicleType;
    @Schema(description = "Nivel de precios del vehículo", example = "PREMIUM")
    private String pricingTier;
    @Schema(description = "Tarifa diaria del vehículo", example = "50.00")
    private BigDecimal dailyRate;
    @Schema(description = "Nivel de precios elegido para el alquiler", example = "STANDARD")
    private PricingTier chosenPricingTier;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Fecha de inicio es requerida")
    @Schema(description = "Fecha y hora de inicio del alquiler", example = "2023-07-21 10:00:00")
    private LocalDateTime startDate;
   
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Fecha de fin es requerida")
    @Schema(description = "Fecha y hora de fin del alquiler", example = "2023-07-25 10:00:00")
    private LocalDateTime endDate;
   
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de precio inválido")
    @Schema(description = "Precio total del alquiler", example = "200.00")
    private BigDecimal totalPrice;
    @Schema(description = "Fecha y hora de creación del registro de alquiler", example = "2023-07-21 09:50:00")
    private LocalDateTime createdAt;

    public void setId(Long id) {
        this.id = id;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public void setVehicleBrand(String vehicleBrand) {
        this.vehicleBrand = vehicleBrand;
    }

    public void setRentalStatus(RentalStatus rentalStatus) {
        this.rentalStatus = rentalStatus;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice != null ? totalPrice.setScale(2, RoundingMode.HALF_UP) : null;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public void setPricingTier(String pricingTier) {
        this.pricingTier = pricingTier;
    }

    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate != null ? dailyRate.setScale(2, RoundingMode.HALF_UP) : null;
    }

    public void setChosenPricingTier(PricingTier chosenPricingTier) {
        this.chosenPricingTier = chosenPricingTier;
    }
}