package com.alquiler.car_rent.commons.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserEntityRequest {
	
	@NotNull
	private String email;
	
	@NotNull
	private String password;
	

}
