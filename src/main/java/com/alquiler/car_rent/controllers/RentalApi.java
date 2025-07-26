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
import com.alquiler.car_rent.commons.dtos.RentalDto;
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
		name = "Rental Management",
		description = "Operaciones para gestionar alquileres de vehículos"
)
@RequestMapping(ApiPathConstants.V1_ROUTE + ApiPathConstants.RENTAL_ROUTE)
public interface RentalApi {

	@Operation(
			summary = "Crear nuevo alquiler",
			description = "Registra un nuevo contrato de alquiler. Cambia el estado del vehículo a RENTED.",
			security = @SecurityRequirement(name = "JWT"),
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Alquiler creado exitosamente",
							content = @Content(mediaType = "application/json")
					),
					@ApiResponse(
							responseCode = "400",
							description = "Datos inválidos o vehículo no disponible",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = {
											@ExampleObject(
													name = "Vehículo no disponible",
													value = "{\"message\": \"El vehiculo no está disponible para alquiler\", \"status\": 400}"
											),
											@ExampleObject(
													name = "Formato fecha inválido",
													value = "{\"message\": \"Formato de fecha de alquiler no válido\", \"status\": 400}"
											)
									}
							)
					),
					@ApiResponse(
							responseCode = "404",
							description = "Cliente o vehículo no encontrado",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = @ExampleObject(
											value = "{\"message\": \"Cliente No Encontrado por ID: 999\", \"status\": 404}"
									)
							)
					)
			}
	)
	@PostMapping
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'USERS', 'ADMIN')")
	ResponseEntity<RentalDto> createRental(@RequestBody RentalDto rentalDto);

	@Operation(
			summary = "Listar todos los alquileres",
			description = "Obtiene todos los contratos de alquiler registrados",
			security = @SecurityRequirement(name = "JWT"),
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Lista de alquileres obtenida",
							content = @Content(mediaType = "application/json")
					)
			}
	)
	@GetMapping
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'USERS', 'ADMIN')")
	ResponseEntity<List<RentalDto>> getAllRentals();

	@Operation(
			summary = "Obtener alquiler por ID",
			description = "Recupera los detalles de un alquiler específico",
			security = @SecurityRequirement(name = "JWT"),
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Alquiler encontrado",
							content = @Content(mediaType = "application/json")
					),
					@ApiResponse(
							responseCode = "404",
							description = "Alquiler no encontrado",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = @ExampleObject(
											value = "{\"message\": \"Alquiler no encontrado con ID: 999\", \"status\": 404}"
									)
							)
					)
			}
	)
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'USERS', 'ADMIN')")
	ResponseEntity<RentalDto> getRentalById(
			@Parameter(
					name = "id",
					description = "ID del alquiler a consultar",
					example = "1",
					in = ParameterIn.PATH,
					required = true
			)
			@PathVariable Long id
	);

	@Operation(
			summary = "Actualizar alquiler",
			description = "Modifica los datos de un alquiler existente",
			security = @SecurityRequirement(name = "JWT"),
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Alquiler actualizado exitosamente",
							content = @Content(mediaType = "application/json")
					),
					@ApiResponse(
							responseCode = "400",
							description = "Datos inválidos",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = @ExampleObject(
											value = "{\"message\": \"Formato de fecha de alquiler no válido\", \"status\": 400}"
									)
							)
					),
					@ApiResponse(
							responseCode = "404",
							description = "Alquiler no encontrado",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = @ExampleObject(
											value = "{\"message\": \"Alquiler no encontrado con ID: 999\", \"status\": 404}"
									)
							)
					)
			}
	)
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'USERS', 'ADMIN')")
	ResponseEntity<RentalDto> updateRental(
			@Parameter(
					name = "id",
					description = "ID del alquiler a actualizar",
					example = "1",
					in = ParameterIn.PATH,
					required = true
			)
			@PathVariable Long id,
			@RequestBody RentalDto rentalDto
	);

	@Operation(
			summary = "Cancelar alquiler",
			description = "Cancela un alquiler activo y cambia el estado del vehículo a AVAILABLE",
			security = @SecurityRequirement(name = "JWT"),
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Alquiler cancelado exitosamente",
							content = @Content(mediaType = "application/json")
					),
					@ApiResponse(
							responseCode = "404",
							description = "Alquiler no encontrado",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = @ExampleObject(
											value = "{\"message\": \"Alquiler no encontrado con ID: 999\", \"status\": 404}"
									)
							)
					)
			}
	)
	@PutMapping("/{id}/cancel")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'USERS', 'ADMIN')")
	ResponseEntity<RentalDto> cancelRental(
			@Parameter(
					name = "id",
					description = "ID del alquiler a cancelar",
					example = "1",
					in = ParameterIn.PATH,
					required = true
			)
			@PathVariable Long id
	);

	@Operation(
			summary = "Eliminar alquiler",
			description = "Elimina permanentemente un registro de alquiler",
			security = @SecurityRequirement(name = "JWT"),
			responses = {
					@ApiResponse(
							responseCode = "204",
							description = "Alquiler eliminado exitosamente"
					),
					@ApiResponse(
							responseCode = "404",
							description = "Alquiler no encontrado",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = @ExampleObject(
											value = "{\"message\": \"Alquiler no encontrado con ID: 999\", \"status\": 404}"
									)
							)
					)
			}
	)
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	ResponseEntity<Void> deleteRental(
			@Parameter(
					name = "id",
					description = "ID del alquiler a eliminar",
					example = "1",
					in = ParameterIn.PATH,
					required = true
			)
			@PathVariable Long id
	);
}