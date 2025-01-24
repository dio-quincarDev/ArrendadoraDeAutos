package service;

import java.util.List;
import java.util.Optional;

import commons.entities.Customer;

public interface CustomerService {
	List<Customer>findAllCustomers();
	Optional<Customer>findCustomerById(Long id);
	Customer createCustomer(Customer customer);
	Customer updateCustomer(Long id, Customer  customer);
	void deleteCustomer(Long id);

}
