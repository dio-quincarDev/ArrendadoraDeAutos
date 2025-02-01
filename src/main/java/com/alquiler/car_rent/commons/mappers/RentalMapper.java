package com.alquiler.car_rent.commons.mappers;

import org.mapstruct.Mapper;

import com.alquiler.car_rent.commons.dtos.RentalDto;
import com.alquiler.car_rent.commons.entities.Rental;

@Mapper(componentModel="spring")
public interface RentalMapper {
	RentalDto rentalToDto(Rental rental);
	Rental dtoToRental(RentalDto rentalDto);

}
