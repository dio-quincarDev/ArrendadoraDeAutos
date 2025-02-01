package com.alquiler.car_rent.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;
import com.alquiler.car_rent.commons.dtos.VehicleDto;

@RequestMapping(ApiPathConstants.V1_ROUTE + "/vehicles" )
public interface VehicleApi {
	@PostMapping
	ResponseEntity<VehicleDto>createVehicle(@RequestBody VehicleDto vehicleDto);
	
	@GetMapping
	ResponseEntity<List<VehicleDto>>getAllVehicles();
	
	@GetMapping("/{id}")
	ResponseEntity<VehicleDto>getVehicleById(@PathVariable Long id);
	
	@PutMapping("/{id}")
	ResponseEntity<VehicleDto>updateVehicle(@PathVariable Long id, @RequestBody VehicleDto vehicleDto);
	
	@DeleteMapping("/{id}")
	ResponseEntity<Void>deleteVehicle(Long id);

}
