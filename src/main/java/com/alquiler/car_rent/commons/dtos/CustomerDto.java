package com.alquiler.car_rent.commons.dtos;

import lombok.Data;

@Data
public class CustomerDto {
	private Long id;
	private String name; 
	private String email; 
	private String license;
	private String phone;
	private String customerStatus;
	private String createdAt;
	private String updatedAt;

}
