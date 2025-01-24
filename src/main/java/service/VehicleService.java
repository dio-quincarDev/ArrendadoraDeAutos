package service;

import java.util.List;
import java.util.Optional;

import commons.entities.Vehicle;
import commons.enums.VehicleStatus;

public interface VehicleService {
	List<Vehicle>findAllVehicles();
	Optional<Vehicle>findVehicleById(Long id);
	List<Vehicle>findVehicleByStatus(VehicleStatus status);
	Vehicle createVehicle(Vehicle vehicle);
	Vehicle updateVehicle(Long id, Vehicle vehicle);
	void deleteVehicle(Long id);

}
