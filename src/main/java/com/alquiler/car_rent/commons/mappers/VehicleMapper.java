package com.alquiler.car_rent.commons.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.alquiler.car_rent.commons.dtos.VehicleDto;
import com.alquiler.car_rent.commons.entities.Vehicle;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mapping(target = "actualDailyRate", ignore = true) // Se calculará en @AfterMapping
    VehicleDto vehicleToDto(Vehicle vehicle);

    Vehicle dtoToVehicle(VehicleDto vehicleDto);

    @AfterMapping
    default void calculateActualDailyRate(Vehicle vehicle, @MappingTarget VehicleDto dto) {
        if (vehicle.getVehicleType() == null || vehicle.getPricingTier() == null) {
            return; // No se puede calcular si falta información
        }

        switch (vehicle.getPricingTier()) {
            case PROMOTIONAL:
                dto.setActualDailyRate(vehicle.getVehicleType().getPromotionalRate());
                break;
            case PREMIUM:
                dto.setActualDailyRate(vehicle.getVehicleType().getPremiumRate());
                break;
            case STANDARD:
            default:
                dto.setActualDailyRate(vehicle.getVehicleType().getStandardRate());
                break;
        }
    }
}