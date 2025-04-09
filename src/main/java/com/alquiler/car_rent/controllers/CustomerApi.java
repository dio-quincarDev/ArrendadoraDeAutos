package com.alquiler.car_rent.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;
import com.alquiler.car_rent.commons.dtos.CustomerDto;

@RequestMapping(ApiPathConstants.V1_ROUTE + "/customers")
public interface CustomerApi {
	
	@PostMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	ResponseEntity<CustomerDto>createCustomer(@RequestBody CustomerDto customerDto);
	
	@GetMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	ResponseEntity<List<CustomerDto>>getAllCustomers();
	
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	ResponseEntity<CustomerDto>getCustomerById(@PathVariable Long id);
	
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	ResponseEntity<CustomerDto>updateCustomer(@PathVariable Long id, @RequestBody CustomerDto customerDto);
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	ResponseEntity<Void>deleteCustomer(@PathVariable Long id);
	
	

}
