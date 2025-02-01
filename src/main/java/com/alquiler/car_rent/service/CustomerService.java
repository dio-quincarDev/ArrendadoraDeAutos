package com.alquiler.car_rent.service;

import java.util.List;
import java.util.Optional;

import com.alquiler.car_rent.commons.dtos.CustomerDto;
import com.alquiler.car_rent.commons.entities.Customer;

public interface CustomerService {
	List<CustomerDto>findAllCustomers();
	Optional<CustomerDto>findCustomerById(Long id);
	Customer createCustomer(Customer customer);
	Customer updateCustomer(Long id, Customer  customer);
	void deleteCustomer(Long id);

}
