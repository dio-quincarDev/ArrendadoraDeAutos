package com.alquiler.car_rent.commons.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.alquiler.car_rent.commons.dtos.RentalDto;
import com.alquiler.car_rent.commons.entities.Rental;

@Mapper(componentModel="spring")
public interface RentalMapper {
	
	 @Mappings({
	        @Mapping(source = "customer.id", target = "customerId"),
	        @Mapping(source = "vehicle.id", target = "vehicleId")
	    })
	RentalDto rentalToDto(Rental rental);
	 
	  @Mappings({
	        @Mapping(target = "customer", expression = "java(new Customer(rentalDto.customerId(), null, null, null, null, null, null, null))"),
	        @Mapping(target = "vehicle", expression = "java(new Vehicle(rentalDto.vehicleId(), null, null, 0, null, null, null))")
	    })
	Rental dtoToRental(RentalDto rentalDto);

}
