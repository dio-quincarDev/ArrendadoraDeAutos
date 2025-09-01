package com.alquiler.car_rent.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.commons.enums.VehicleStatus;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>{
	List<Vehicle>findByStatus(VehicleStatus status);
    Optional<Vehicle> findByPlate(String plate);

}
