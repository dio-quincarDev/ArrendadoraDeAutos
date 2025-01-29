package com.alquiler.car_rent.repositories;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.enums.RentalStatus;

public interface RentalRepository extends JpaRepository<Rental, Long> {
	List<Rental>findByRentalStatus(RentalStatus rentalStatus);
	List<Rental>findByRentalStatusAndEndDateBetween(RentalStatus rentalStatus, LocalDateTime start, LocalDateTime end);

}
