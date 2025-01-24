package repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import commons.entities.Rental;
import commons.enums.RentalStatus;

public interface RentalRepository extends JpaRepository<Rental, Long> {
	List<Rental>findByRentalStatus(RentalStatus status);

}
