package com.alquiler.car_rent.commons.mappers;

import org.mapstruct.Mapper;

import com.alquiler.car_rent.commons.dtos.VehicleDto;
import com.alquiler.car_rent.commons.entities.Vehicle;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehicleMapper {
	@Mapping(target = "id", source = "id")
	@Mapping(target = "brand", source = "brand")
	@Mapping(target = "model", source = "model")
	@Mapping(target = "year", source = "year")
	@Mapping(target = "plate", source = "plate")
	@Mapping(target = "status", source = "status")
	@Mapping(target = "createdAt", source = "createdAt")
	VehicleDto vehicleToDto(Vehicle vehicle);

	@Mapping(target = "id", source = "id")
	@Mapping(target = "brand", source = "brand")
	@Mapping(target = "model", source = "model")
	@Mapping(target = "year", source = "year")
	@Mapping(target = "plate", source = "plate")
	@Mapping(target = "status", source = "status")
	@Mapping(target = "createdAt", source = "createdAt")
	Vehicle dtoToVehicle(VehicleDto vehicleDto);

}
