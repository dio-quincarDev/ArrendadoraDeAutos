package controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import commons.constants.ApiPathConstants;
import commons.entities.Vehicle;

@RequestMapping(ApiPathConstants.V1_ROUTE + "/vehicles" )
public interface VehicleApi {
	@PostMapping
	ResponseEntity<Vehicle>createVehicle(@RequestBody Vehicle vehicle);
	
	@GetMapping
	ResponseEntity<List<Vehicle>>getAllVehicles();
	
	@GetMapping("/{id}")
	ResponseEntity<Vehicle>getVehicleById(@PathVariable Long id);
	
	@PutMapping("/{id}")
	ResponseEntity<Vehicle>updateVehicle(@PathVariable Long id, @RequestBody Vehicle vehicle);
	
	@DeleteMapping("/{id]")
	ResponseEntity<Void>deleteVehicle(Long id);

}
