package com.alquiler.car_rent.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.commons.enums.VehicleStatus;
import com.alquiler.car_rent.repositories.VehicleRepository;
import com.alquiler.car_rent.service.VehicleService;

@Service
public class VehicleServiceImpl implements VehicleService {
	private final VehicleRepository vehicleRepository;
	
	public VehicleServiceImpl (VehicleRepository vehicleRepository) {
		this.vehicleRepository = vehicleRepository;
	}

	@Override
	public List<Vehicle> findAllVehicles() {
		
		return vehicleRepository.findAll();
	}

	@Override
	public Optional<Vehicle> findVehicleById(Long id) {
		
		return vehicleRepository.findById(id);
	}

	@Override
	public List<Vehicle> findVehicleByStatus(VehicleStatus status) {
		
		return vehicleRepository.findByStatus(status);
	}

	@Override
	public Vehicle createVehicle(Vehicle vehicle) {
		vehicle.setStatus(VehicleStatus.AVAILABLE);
		return vehicleRepository.save(vehicle);
	}

	@Override
	public Vehicle updateVehicle(Long id, Vehicle vehicle) {
		return vehicleRepository.findById(id)
				.map(existingVehicle-> {
					existingVehicle.setBrand(vehicle.getBrand());
					existingVehicle.setModel(vehicle.getModel());
					existingVehicle.setPlate(vehicle.getPlate());
					existingVehicle.setStatus(vehicle.getStatus());
					return vehicleRepository.save(existingVehicle);
				})
		.orElseThrow(()-> new IllegalArgumentException("Vehiculo no encontrado con el ID" + id));
	}

	@Override
	public void deleteVehicle(Long id) {
		   if (!vehicleRepository.existsById(id)) {
	            throw new IllegalArgumentException("Vehículo no encontrado con ID: " + id);
	        }
	        vehicleRepository.deleteById(id);
	}
	
}
