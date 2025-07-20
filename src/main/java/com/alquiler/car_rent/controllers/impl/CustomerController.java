package com.alquiler.car_rent.controllers.impl;

import com.alquiler.car_rent.commons.dtos.CustomerDto;
import com.alquiler.car_rent.controllers.CustomerApi;
import com.alquiler.car_rent.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CustomerController implements CustomerApi{
	
	private final CustomerService customerService;
	
	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@Override
	public ResponseEntity<CustomerDto> createCustomer(CustomerDto customerDto) {
		CustomerDto createdCustomer = customerService.createCustomer(customerDto);
		
		return ResponseEntity.ok(createdCustomer);
	}

	@Override
	public ResponseEntity<List<CustomerDto>> getAllCustomers() {
		List<CustomerDto> customers = customerService.findAllCustomers();
		return ResponseEntity.ok(customers);
	}

	@Override
	public ResponseEntity<CustomerDto> getCustomerById(Long id) {
		CustomerDto customerDto = customerService.findCustomerById(id);
		return ResponseEntity.ok(customerDto);
	}

	@Override
	public ResponseEntity<CustomerDto> updateCustomer(Long id, CustomerDto customerDto) {
		CustomerDto updatedCustomer = customerService.updateCustomer(id, customerDto);
		return ResponseEntity.ok(updatedCustomer);
	}

	@Override
	public ResponseEntity<Void> deleteCustomer(Long id) {
		customerService.deleteCustomer(id);
		
		return ResponseEntity.noContent().build();
	}

}
