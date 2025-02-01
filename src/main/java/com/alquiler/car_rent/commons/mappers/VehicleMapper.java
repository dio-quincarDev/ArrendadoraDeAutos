package com.alquiler.car_rent.commons.mappers;

import org.mapstruct.Mapper;

import com.alquiler.car_rent.commons.dtos.VehicleDto;
import com.alquiler.car_rent.commons.entities.Vehicle;

@Mapper(componentModel = "spring")
public interface VehicleMapper {
	VehicleDto vehicleToDto(Vehicle vehicle);
	Vehicle dtoToVehicle(VehicleDto vehicleDto);

}
