package controllers.impl;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import commons.entities.Customer;
import controllers.CustomerApi;
import service.CustomerService;

@RestController
public class CustomerController implements CustomerApi{
	
	private final CustomerService customerService;
	
	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@Override
	public ResponseEntity<Customer> createCustomer(Customer customer) {
		Customer createdCustomer = customerService.createCustomer(customer);
		
		return ResponseEntity.ok(createdCustomer);
	}

	@Override
	public ResponseEntity<List<Customer>> getAllCustomers() {
		List<Customer> customers = customerService.findAllCustomers();
		return ResponseEntity.ok(customers);
	}

	@Override
	public ResponseEntity<Customer> getCustomerById(Long id) {
		
		return customerService.findCustomerById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<Customer> updateCustomer(Long id, Customer customer) {
		Customer updatedCustomer = customerService.updateCustomer(id, customer);
		return ResponseEntity.ok(updatedCustomer);
	}

	@Override
	public ResponseEntity<Customer> deleteCustomer(Long id) {
		customerService.deleteCustomer(id);
		
		return ResponseEntity.noContent().build();
	}

}
