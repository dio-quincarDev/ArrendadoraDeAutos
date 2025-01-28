package com.alquiler.car_rent.commons.dtos;

import lombok.Data;

@Data
public class VehicleDto {
private Long id;
private String brand;
private String model;
private int year;
private String plate;
private String status;
private String createdAt;

}
