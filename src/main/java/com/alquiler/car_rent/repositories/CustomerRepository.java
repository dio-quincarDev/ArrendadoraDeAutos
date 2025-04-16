package com.alquiler.car_rent.repositories;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alquiler.car_rent.commons.entities.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long>{
	boolean existsByEmail(String email);
	
	  long countByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

}
