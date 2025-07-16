package com.alquiler.car_rent.commons.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.alquiler.car_rent.commons.enums.PricingTier;
import com.alquiler.car_rent.commons.enums.VehicleStatus;
import com.alquiler.car_rent.commons.enums.VehicleType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleDto {
    private Long id;
    private String brand;
    private String model;
    private Integer year;
    private String plate;
    private VehicleStatus status;
    private VehicleType vehicleType;
    private PricingTier pricingTier;
    private BigDecimal actualDailyRate;
    private LocalDateTime createdAt;
}