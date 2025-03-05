package com.alquiler.car_rent.commons.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.alquiler.car_rent.commons.dtos.RentalDto;
import com.alquiler.car_rent.commons.entities.Rental;

@Mapper(componentModel="spring")
public interface RentalMapper {
    RentalMapper INSTANCE = Mappers.getMapper(RentalMapper.class);

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName") // Mapea el nombre del cliente
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "vehicle.brand", target = "vehicleBrand") // Mapea el modelo del vehículo
    @Mapping(source = "vehicle.model", target = "vehicleModel")
    RentalDto rentalToDto(Rental rental);

    @Mapping(target = "customer", ignore = true) // Evita problemas al mapear el objeto completo
    @Mapping(target = "vehicle", ignore = true)
    Rental dtoToRental(RentalDto rentalDto);
}
