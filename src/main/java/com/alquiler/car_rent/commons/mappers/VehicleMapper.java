package com.alquiler.car_rent.commons.mappers;

import com.alquiler.car_rent.commons.dtos.VehicleDto;
import com.alquiler.car_rent.commons.entities.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VehicleMapper {

    VehicleDto vehicleToDto(Vehicle vehicle);

    Vehicle dtoToVehicle(VehicleDto vehicleDto);

    void updateVehicleFromDto(VehicleDto dto, @MappingTarget Vehicle entity);
}
