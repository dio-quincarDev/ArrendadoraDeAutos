package com.alquiler.car_rent.commons.entities;

import java.time.LocalDateTime;

import com.alquiler.car_rent.commons.enums.VehicleStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Vehicle {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@NotBlank(message = "Marca Obligatoria")
	private String brand;
	
	@NotBlank(message = "Modelo de Auto")
	private String model;
	
	private int year;
	
	@NotBlank(message = "Matricula del Auto Obligatoria")
	private String plate;
	
	@Enumerated(EnumType.STRING)
	private VehicleStatus status;
	
	private LocalDateTime createdAt;
	
	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		if(this.status == null) {
			this.status = VehicleStatus.AVAILABLE;
		}
	}
	
	
	

}
