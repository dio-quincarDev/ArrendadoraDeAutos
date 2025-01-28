package com.alquiler.car_rent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication


public class CarRentApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarRentApplication.class, args);
	}

}
