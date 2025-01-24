package repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import commons.entities.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long>{
	boolean existsByEmail(String email);

}
