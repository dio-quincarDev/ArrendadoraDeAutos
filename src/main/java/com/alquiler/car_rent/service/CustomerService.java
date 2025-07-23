package com.alquiler.car_rent.service;

import com.alquiler.car_rent.commons.dtos.CustomerDto;

import java.util.List;

public interface CustomerService {
	List<CustomerDto> findAllCustomers();
	CustomerDto findCustomerById(Long id);
	CustomerDto createCustomer(CustomerDto customerdto);
	CustomerDto updateCustomer(Long id, CustomerDto  customerDto);
	void deleteCustomer(Long id);

}
