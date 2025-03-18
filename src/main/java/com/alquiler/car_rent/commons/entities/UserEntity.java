package com.alquiler.car_rent.commons.entities;

import com.alquiler.car_rent.commons.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class UserEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique =  true, nullable= false)
	private String username;
	
	@Column(unique =  true, nullable= false)
	private String password;
	
	@Enumerated(EnumType.STRING)
	private Role role;
}
