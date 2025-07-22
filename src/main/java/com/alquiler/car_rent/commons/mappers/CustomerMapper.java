package com.alquiler.car_rent.commons.mappers;

import org.mapstruct.Mapper;

import com.alquiler.car_rent.commons.dtos.CustomerDto;
import com.alquiler.car_rent.commons.entities.Customer;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
	CustomerDto customerToDto(Customer customer);

	Customer dtoToCustomer(CustomerDto customerDto);


}
