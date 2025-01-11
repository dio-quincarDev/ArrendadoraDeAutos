package commons.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import commons.enums.RentalStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rentals")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Rental {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
    @ManyToOne
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;
	
    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
	private Vehicle vehicle;
	
	@Enumerated(EnumType.STRING)
	private RentalStatus rentalStatus;
	
	private LocalDateTime startDate;
	
	private LocalDateTime endDate;
	
	private BigDecimal totalPrice;
	
	private LocalDateTime createdAt;
	
	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		if(this.rentalStatus == null) {
			this.rentalStatus = RentalStatus.PENDING;
			
		}
	}
	

}
