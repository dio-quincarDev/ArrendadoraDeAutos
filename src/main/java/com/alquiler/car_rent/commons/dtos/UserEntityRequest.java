package com.alquiler.car_rent.commons.dtos;

import com.alquiler.car_rent.commons.enums.Role;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Solicitud para la creación o actualización de un usuario")
public class UserEntityRequest {
	 
	@Email(message = "El formato del email no es válido")
	@NotBlank(message = "El email no puede estar vacío")
    @Schema(description = "Correo electrónico del usuario", example = "usuario@example.com")
    private String email;

	@NotBlank(message = "La contraseña no puede estar vacío")
	@Schema(description = "Contraseña del usuario", example = "passwordSegura123")
	private String password;
	
	@NotNull(message = "El campo 'role' no puede estar vacío")
    @Schema(description = "Rol del usuario (ADMIN o USERS)", example = "USERS")
    private Role role; // Nuevo campo para definir roles
	
	@NotBlank(message = "El username no puede estar vacío")
    @Schema(description = "Nombre de usuario", example = "usuario123")
    private String username;


}

