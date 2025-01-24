package repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import commons.entities.Vehicle;
import commons.enums.VehicleStatus;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>{
	List<Vehicle>findByStatus(VehicleStatus status);

}
