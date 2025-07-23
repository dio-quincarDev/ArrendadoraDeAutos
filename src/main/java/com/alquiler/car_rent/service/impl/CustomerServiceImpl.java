package com.alquiler.car_rent.service.impl;

import com.alquiler.car_rent.commons.dtos.CustomerDto;
import com.alquiler.car_rent.commons.entities.Customer;
import com.alquiler.car_rent.commons.mappers.CustomerMapper;
import com.alquiler.car_rent.exceptions.BadRequestException;
import com.alquiler.car_rent.exceptions.NotFoundException;
import com.alquiler.car_rent.repositories.CustomerRepository;
import com.alquiler.car_rent.service.CustomerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {
	
	private final CustomerRepository customerRepository;
	private final CustomerMapper customerMapper;
	
	public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper) {
		this.customerRepository = customerRepository;
		this.customerMapper = customerMapper;
	}

	@Override
	public List<CustomerDto> findAllCustomers() {
		
		return customerRepository.findAll()
				.stream()
				.map(customerMapper::customerToDto)
				.toList();
	}

	@Override
	public CustomerDto findCustomerById(Long id) {
		return customerRepository.findById(id)
                .map(customerMapper::customerToDto)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado con ID: " + id));
	}

	

	@Override
	public CustomerDto createCustomer(CustomerDto customerDto) {
		if (customerRepository.existsByEmail(customerDto.email())) {
            throw new BadRequestException("El correo ya está registrado.");
        }
		Customer customer = customerMapper.dtoToCustomer(customerDto);
        return customerMapper.customerToDto(customerRepository.save(customer));
	}
	

	@Override
	public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
		  return customerRepository.findById(id)
	                .map(existingCustomer -> {
	                    existingCustomer.setName(customerDto.name());
	                    existingCustomer.setEmail(customerDto.email());
	                    existingCustomer.setPhone(customerDto.phone());
	                    existingCustomer.setLicense(customerDto.license());
	                    customerRepository.save(existingCustomer);
	                    return customerMapper.customerToDto(customerRepository.save(existingCustomer));
	                })
	                .orElseThrow(() -> new NotFoundException("Cliente no encontrado con ID: " + id));
	}
	

	@Override
	public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new NotFoundException("Cliente no encontrado con ID: " + id);
        }
        customerRepository.deleteById(id);
    
	}


}
