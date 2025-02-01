package com.alquiler.car_rent.service;

import java.util.List;
import java.util.Optional;
import com.alquiler.car_rent.commons.dtos.CustomerDto;

public interface CustomerService {
	List<CustomerDto> findAllCustomers();
	Optional<CustomerDto>findCustomerById(Long id);
	CustomerDto createCustomer(CustomerDto customerdto);
	CustomerDto updateCustomer(Long id, CustomerDto  customerDto);
	void deleteCustomer(Long id);

}
