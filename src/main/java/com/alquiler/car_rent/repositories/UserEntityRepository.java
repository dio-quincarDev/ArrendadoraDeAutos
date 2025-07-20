package com.alquiler.car_rent.repositories;

import java.util.Optional;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import com.alquiler.car_rent.commons.entities.UserEntity;

import com.alquiler.car_rent.commons.enums.Role;

public interface UserEntityRepository extends JpaRepository<UserEntity, Long > {
	
	Optional<UserEntity> findByEmail(String email);

    boolean existsByRole(Role role);

    Optional<Object> findByUsername(@NotBlank(message = "El username no puede estar vacío") String username);
}
