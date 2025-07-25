package com.alquiler.car_rent.controllers.impl;

import com.alquiler.car_rent.commons.dtos.VehicleDto;
import com.alquiler.car_rent.controllers.VehicleApi;
import com.alquiler.car_rent.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VehicleController implements VehicleApi {
	
	private final VehicleService vehicleService;
	
	 public VehicleController(VehicleService vehicleService) {
	        this.vehicleService = vehicleService;
	    }

	@Override
	public ResponseEntity<VehicleDto> createVehicle(@Valid @RequestBody VehicleDto vehicleDto) {
		VehicleDto createdVehicle = vehicleService.createVehicle(vehicleDto);
		return new ResponseEntity<>(createdVehicle, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<List<VehicleDto>> getAllVehicles() {
		List<VehicleDto> vehicles = vehicleService.findAllVehicles();
		return ResponseEntity.ok(vehicles);
	}

	@Override
	public ResponseEntity<VehicleDto> getVehicleById(Long id) {
		VehicleDto vehicleDto = vehicleService.findVehicleById(id);
		return ResponseEntity.ok(vehicleDto);
	}

	@Override
	public ResponseEntity<VehicleDto> updateVehicle(Long id, @Valid @RequestBody VehicleDto vehicleDto) {
		VehicleDto updatedVehicle = vehicleService.updateVehicle(id, vehicleDto);
		return ResponseEntity.ok(updatedVehicle);
	}

	@Override
	public ResponseEntity<Void> deleteVehicle(Long id) {
		vehicleService.deleteVehicle(id);
		return ResponseEntity.noContent().build();
	}

}
