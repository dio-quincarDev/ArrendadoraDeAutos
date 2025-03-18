package com.alquiler.car_rent.commons.mappers;

import org.mapstruct.Mapper;

import com.alquiler.car_rent.commons.dtos.CustomerDto;
import com.alquiler.car_rent.commons.entities.Customer;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
	@Mapping(target = "id", source = "id")
	@Mapping(target = "name", source = "name")
	@Mapping(target = "email", source = "email")
	@Mapping(target = "license", source = "license")
	@Mapping(target = "createdAt", source = "createdAt")
	@Mapping(target = "updatedAt", source = "updatedAt")
	@Mapping(target = "phone", source = "phone")
	@Mapping(target = "customerStatus", source = "customerStatus")
	CustomerDto customerToDto(Customer customer);

	@Mapping(target = "id", source = "id")
	@Mapping(target = "name", source = "name")
	@Mapping(target = "email", source = "email")
	@Mapping(target = "license", source = "license")
	@Mapping(target = "createdAt", source = "createdAt")
	@Mapping(target = "updatedAt", source = "updatedAt")
	@Mapping(target = "phone", source = "phone")
	@Mapping(target = "customerStatus", source = "customerStatus")
	Customer dtoToCustomer(CustomerDto customerDto);
	
}
