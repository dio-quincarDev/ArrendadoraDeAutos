package com.alquiler.car_rent.commons.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.alquiler.car_rent.commons.dtos.RentalDto;
import com.alquiler.car_rent.commons.entities.Rental;

@Mapper(componentModel = "spring")
public interface RentalMapper {

    RentalMapper INSTANCE = Mappers.getMapper(RentalMapper.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "vehicle.brand", target = "vehicleBrand")
    @Mapping(source = "vehicle.model", target = "vehicleModel")
    @Mapping(source = "rentalStatus", target = "rentalStatus")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "totalPrice", target = "totalPrice")
    @Mapping(source = "createdAt", target = "createdAt")
    RentalDto rentalToDto(Rental rental);

    @Mapping(target = "customer", ignore = true) // Ignora el mapeo del cliente
    @Mapping(target = "vehicle", ignore = true)  // Ignora el mapeo del vehículo
    @Mapping(target = "id", ignore = true)       // Ignora el ID para evitar sobrescritura
    Rental dtoToRental(RentalDto rentalDto);
}