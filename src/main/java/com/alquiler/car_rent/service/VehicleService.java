package com.alquiler.car_rent.service;

import com.alquiler.car_rent.commons.dtos.VehicleDto;
import com.alquiler.car_rent.commons.enums.VehicleStatus;

import java.util.List;

public interface VehicleService {
	List<VehicleDto>findAllVehicles();
	VehicleDto findVehicleById(Long id);
	List<VehicleDto>findVehicleByStatus(VehicleStatus status);
	VehicleDto createVehicle(VehicleDto vehicleDto);
	VehicleDto updateVehicle(Long id, VehicleDto vehicleDto);
	void deleteVehicle(Long id);

}
