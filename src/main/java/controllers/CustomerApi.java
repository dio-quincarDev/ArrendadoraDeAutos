package controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import commons.constants.ApiPathConstants;
import commons.entities.Customer;

@RequestMapping(ApiPathConstants.V1_ROUTE + "/customers")
public interface CustomerApi {
	
	@PostMapping
	ResponseEntity<Customer>createCustomer(@RequestBody Customer customer);
	
	@GetMapping
	ResponseEntity<List<Customer>>getAllCustomers();
	
	@GetMapping("/{id}")
	ResponseEntity<Customer>getCustomerById(@PathVariable Long id);
	
	@PutMapping("/{id}")
	ResponseEntity<Customer>updateCustomer(@PathVariable Long id, Customer customer);
	
	@DeleteMapping("/{id}")
	ResponseEntity<Customer>deleteCustomer(@PathVariable Long id);
	
	

}
