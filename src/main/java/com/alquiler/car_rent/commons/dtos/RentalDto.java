package com.alquiler.car_rent.commons.dtos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import com.alquiler.car_rent.commons.enums.RentalStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;


@Getter
public class RentalDto {

    // Getters y Setters
    @NotNull(message = "ID no puede ser nulo")
    private Long id;
	@NotNull(message = "Customer ID es requerido")
    private Long customerId;
	@NotBlank(message = "Nombre de cliente es obligatorio")
    private String customerName;
    @NotNull(message = "Vehicle ID es requerido")
    private Long vehicleId;
    
    private String vehicleModel;
    private String vehicleBrand;
    private RentalStatus rentalStatus;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Fecha de inicio es requerida")
    private LocalDateTime startDate;
   
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Fecha de fin es requerida")
    private LocalDateTime endDate;
   
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de precio inválido")
    private BigDecimal totalPrice;
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
}