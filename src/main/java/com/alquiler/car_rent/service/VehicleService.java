package com.alquiler.car_rent.service;

import java.util.List;
import java.util.Optional;

import com.alquiler.car_rent.commons.dtos.VehicleDto;
import com.alquiler.car_rent.commons.enums.VehicleStatus;

public interface VehicleService {
	List<VehicleDto>findAllVehicles();
	Optional<VehicleDto>findVehicleById(Long id);
	List<VehicleDto>findVehicleByStatus(VehicleStatus status);
	VehicleDto createVehicle(VehicleDto vehicleDto);
	VehicleDto updateVehicle(Long id, VehicleDto vehicleDto);
	void deleteVehicle(Long id);

}
