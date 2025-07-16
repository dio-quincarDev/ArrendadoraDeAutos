package com.alquiler.car_rent.commons.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.alquiler.car_rent.commons.dtos.VehicleDto;
import com.alquiler.car_rent.commons.entities.Vehicle;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    VehicleDto vehicleToDto(Vehicle vehicle);

    Vehicle dtoToVehicle(VehicleDto vehicleDto);

    
}