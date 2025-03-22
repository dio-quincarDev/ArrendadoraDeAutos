package com.alquiler.car_rent.commons.dtos;

import com.alquiler.car_rent.commons.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
	 
	@Email(message = "El formato del email no es válido")
	@NotBlank(message = "El email no puede estar vacío")
    private String email;

	@NotBlank(message = "La contraseña no puede estar vacía")
	private String password;
	
	@NotNull(message = "El campo 'role' no puede estar vacío")
    private Role role; // Nuevo campo para definir roles
	
	@NotBlank(message = "El username no puede estar vacío")
    private String username;


}
