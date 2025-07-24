package com.alquiler.car_rent.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;
import com.alquiler.car_rent.commons.dtos.CustomerDto;
import com.alquiler.car_rent.exceptions.GlobalExceptionHandler.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
		name = "Customer Management",
		description = "Operaciones CRUD para la gestión de clientes"
)
@RequestMapping(ApiPathConstants.V1_ROUTE + ApiPathConstants.CUSTOMER_ROUTE)
public interface CustomerApi {

	@Operation(
			summary = "Crear nuevo cliente",
			description = "Registra un nuevo cliente en el sistema",
			security = @SecurityRequirement(name = "JWT"),
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Cliente creado exitosamente",
							content = @Content(mediaType = "application/json")
					),
					@ApiResponse(
							responseCode = "400",
							description = "Datos del cliente inválidos",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = @ExampleObject(
											value = "{\"message\": \"El email ya está registrado\", \"status\": 400}"
									)
							)
					),
					@ApiResponse(
							responseCode = "403",
							description = "Acceso denegado (requiere rol ADMIN)"
					)
			}
	)
	@PostMapping
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USERS')")
	ResponseEntity<CustomerDto> createCustomer(@RequestBody CustomerDto customerDto);

	@Operation(
			summary = "Obtener todos los clientes",
			description = "Retorna una lista completa de clientes registrados",
			security = @SecurityRequirement(name = "JWT"),
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Lista de clientes obtenida exitosamente",
							content = @Content(mediaType = "application/json")
					),
					@ApiResponse(
							responseCode = "403",
							description = "Acceso denegado (requiere rol USERS o ADMIN)"
					)
			}
	)
	@GetMapping
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'USERS', 'ADMIN')")
	ResponseEntity<List<CustomerDto>> getAllCustomers();

	@Operation(
			summary = "Obtener cliente por ID",
			description = "Recupera los detalles de un cliente específico",
			security = @SecurityRequirement(name = "JWT"),
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Cliente encontrado",
							content = @Content(mediaType = "application/json")
					),
					@ApiResponse(
							responseCode = "404",
							description = "Cliente no encontrado",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = @ExampleObject(
											value = "{\"message\": \"Cliente no encontrado con ID: 999\", \"status\": 404}"
									)
							)
					),
					@ApiResponse(
							responseCode = "403",
							description = "Acceso denegado (requiere rol USERS o ADMIN)"
					)
			}
	)
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'USERS', 'ADMIN')")
	ResponseEntity<CustomerDto> getCustomerById(
			@Parameter(
					name = "id",
					description = "ID único del cliente",
					example = "1",
					in = ParameterIn.PATH,
					required = true
			)
			@PathVariable Long id
	);

	@Operation(
			summary = "Actualizar cliente",
			description = "Modifica los datos de un cliente existente",
			security = @SecurityRequirement(name = "JWT"),
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Cliente actualizado exitosamente",
							content = @Content(mediaType = "application/json")
					),
					@ApiResponse(
							responseCode = "400",
							description = "Datos inválidos o cliente no encontrado",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = @ExampleObject(
											value = "{\"message\": \"Cliente no encontrado con ID: 999\", \"status\": 400}"
									)
							)
					),
					@ApiResponse(
							responseCode = "403",
							description = "Acceso denegado (requiere rol ADMIN o USER)"
					)
			}
	)
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'USERS', 'ADMIN')")
	ResponseEntity<CustomerDto> updateCustomer(
			@Parameter(
					name = "id",
					description = "ID del cliente a actualizar",
					example = "1",
					in = ParameterIn.PATH,
					required = true
			)
			@PathVariable Long id,
			@RequestBody CustomerDto customerDto
	);

	@Operation(
			summary = "Eliminar cliente",
			description = "Elimina permanentemente un cliente del sistema",
			security = @SecurityRequirement(name = "JWT"),
			responses = {
					@ApiResponse(
							responseCode = "204",
							description = "Cliente eliminado exitosamente"
					),
					@ApiResponse(
							responseCode = "400",
							description = "ID inválido o cliente no encontrado",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = @ExampleObject(
											value = "{\"message\": \"Cliente no encontrado con ID: 999\", \"status\": 400}"
									)
							)
					),
					@ApiResponse(
							responseCode = "403",
							description = "Acceso denegado (requiere rol ADMIN)"
					)
			}
	)
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
	ResponseEntity<Void> deleteCustomer(
			@Parameter(
					name = "id",
					description = "ID del cliente a eliminar",
					example = "1",
					in = ParameterIn.PATH,
					required = true
			)
			@PathVariable Long id
	);
}