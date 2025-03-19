package com.alquiler.car_rent.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alquiler.car_rent.commons.entities.UserEntity;

public interface UserEntityRepository extends JpaRepository<UserEntity, Long > {
	
	Optional<UserEntity> findByEmail(String email);

}
