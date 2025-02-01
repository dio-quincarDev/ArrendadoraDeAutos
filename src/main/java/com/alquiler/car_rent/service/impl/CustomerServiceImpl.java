package com.alquiler.car_rent.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.alquiler.car_rent.commons.dtos.CustomerDto;
import com.alquiler.car_rent.commons.entities.Customer;
import com.alquiler.car_rent.repositories.CustomerRepository;
import com.alquiler.car_rent.service.CustomerService;

@Service
public class CustomerServiceImpl implements CustomerService {
	
	private final CustomerRepository customerRepository;
	
	public CustomerServiceImpl(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Override
	public List<CustomerDto> findAllCustomers() {
		
		return customerRepository.findAll()
				.stream()
				.map(CustomerDto::fromEntity)
				.toList();
	}

	@Override
	public Optional<CustomerDto> findCustomerById(Long id) {
	
		return customerRepository.findById(id).map(CustomerDto::fromEntity);
	}

	

	@Override
	public Customer createCustomer(Customer customer) {
		if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException("El correo ya está registrado.");
        }
        return customerRepository.save(customer);
	}

	@Override
	public Customer updateCustomer(Long id, Customer customer) {
		  return customerRepository.findById(id)
	                .map(existingCustomer -> {
	                    existingCustomer.setName(customer.getName());
	                    existingCustomer.setEmail(customer.getEmail());
	                    existingCustomer.setPhone(customer.getPhone());
	                    existingCustomer.setLicense(customer.getLicense());
	                    return customerRepository.save(existingCustomer);
	                })
	                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));
	}
	

	@Override
	public void deleteCustomer(Long id) {     if (!customerRepository.existsById(id)) {
        throw new IllegalArgumentException("Cliente no encontrado con ID: " + id);
    }
    customerRepository.deleteById(id);
    
	}


}
