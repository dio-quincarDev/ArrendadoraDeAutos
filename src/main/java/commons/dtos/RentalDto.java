package commons.dtos;

import java.math.BigDecimal;

import lombok.Data;


@Data
public class RentalDto {

	private Long id;
	private Long customerId;
	private Long vehicleId;
	private String rentalStatus;
	private String startDate;
	private String endDate;
	private BigDecimal totalPrice;
	private String CreatedAt;
}
