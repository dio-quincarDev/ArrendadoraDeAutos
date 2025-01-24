package controllers.impl;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import commons.entities.Vehicle;
import controllers.VehicleApi;
import service.VehicleService;

@RestController
public class VehicleController implements VehicleApi {
	
	private final VehicleService vehicleService;
	
	 public VehicleController(VehicleService vehicleService) {
	        this.vehicleService = vehicleService;
	    }

	@Override
	public ResponseEntity<Vehicle> createVehicle(Vehicle vehicle) {
		Vehicle createdVehicle = vehicleService.createVehicle(vehicle);
		return ResponseEntity.ok(createdVehicle);
	}

	@Override
	public ResponseEntity<List<Vehicle>> getAllVehicles() {
		List<Vehicle> vehicles = vehicleService.findAllVehicles();
		return ResponseEntity.ok(vehicles);
	}

	@Override
	public ResponseEntity<Vehicle> getVehicleById(Long id) {
		return vehicleService.findVehicleById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<Vehicle> updateVehicle(Long id, Vehicle vehicle) {
		Vehicle updatedVehicle = vehicleService.updateVehicle(id, vehicle);
		return ResponseEntity.ok(updatedVehicle);
	}

	@Override
	public ResponseEntity<Void> deleteVehicle(Long id) {
		vehicleService.deleteVehicle(id);
		return ResponseEntity.noContent().build();
	}

}
