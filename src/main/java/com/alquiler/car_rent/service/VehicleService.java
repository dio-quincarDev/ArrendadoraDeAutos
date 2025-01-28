package com.alquiler.car_rent.service;

import java.util.List;
import java.util.Optional;

import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.commons.enums.VehicleStatus;

public interface VehicleService {
	List<Vehicle>findAllVehicles();
	Optional<Vehicle>findVehicleById(Long id);
	List<Vehicle>findVehicleByStatus(VehicleStatus status);
	Vehicle createVehicle(Vehicle vehicle);
	Vehicle updateVehicle(Long id, Vehicle vehicle);
	void deleteVehicle(Long id);

}
