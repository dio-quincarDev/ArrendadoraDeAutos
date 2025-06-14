package com.alquiler.car_rent.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import com.alquiler.car_rent.commons.dtos.VehicleDto;
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
		name = "Vehicle Management",
		description = "Operaciones CRUD para la gestión de vehículos en el sistema de alquiler"
)
@RequestMapping(ApiPathConstants.V1_ROUTE + "/vehicles")
public interface VehicleApi {

	@Operation(
			summary = "Crear un nuevo vehículo",
			description = "Registra un nuevo vehículo en la flota. El estado se establece como 'AVAILABLE' automáticamente.",
			security = @SecurityRequirement(name = "JWT"),
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Vehículo creado exitosamente",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = VehicleDto.class)
							)
					),
					@ApiResponse(
							responseCode = "400",
							description = "Datos de entrada inválidos",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = @ExampleObject(
											value = "{\"message\": \"La placa ya está registrada\", \"status\": 400}"
									)
							)
					),
					@ApiResponse(
							responseCode = "403",
							description = "Acceso denegado - Se requiere rol ADMIN",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class)
							)
					)
			}
	)
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<VehicleDto> createVehicle(
			@RequestBody VehicleDto vehicleDto
	);

	@Operation(
			summary = "Obtener todos los vehículos",
			description = "Retorna una lista completa de todos los vehículos registrados en el sistema",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Lista de vehículos obtenida exitosamente",
							content = @Content(
									mediaType = "application/json",
									array = @ArraySchema(schema = @Schema(implementation = VehicleDto.class))
							)
					)
			}
	)
	@GetMapping
	ResponseEntity<List<VehicleDto>> getAllVehicles();

	@Operation(
			summary = "Obtener vehículo por ID",
			description = "Recupera los detalles de un vehículo específico usando su ID único",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Vehículo encontrado",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = VehicleDto.class)
							)
					),
					@ApiResponse(
							responseCode = "404",
							description = "Vehículo no encontrado",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = @ExampleObject(
											value = "{\"message\": \"Vehículo no encontrado con el ID: 999\", \"status\": 404}"
									)
							)
					)
			}
	)
	@GetMapping("/{id}")
	ResponseEntity<VehicleDto> getVehicleById(
			@Parameter(
					name = "id",
					description = "ID único del vehículo",
					example = "1",
					in = ParameterIn.PATH,
					required = true
			)
			@PathVariable Long id
	);

	@Operation(
			summary = "Actualizar vehículo",
			description = "Actualiza todos los campos de un vehículo existente",
			security = @SecurityRequirement(name = "JWT"),
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Vehículo actualizado exitosamente",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = VehicleDto.class)
							)
					),
					@ApiResponse(
							responseCode = "400",
							description = "Datos inválidos o vehículo no encontrado",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = @ExampleObject(
											value = "{\"message\": \"Vehículo no encontrado con el ID: 999\", \"status\": 400}"
									)
							)
					),
					@ApiResponse(
							responseCode = "403",
							description = "Acceso denegado - Se requiere rol ADMIN o USER",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class)
							)
					)
			}
	)
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('USERS', 'ADMIN')")
	ResponseEntity<VehicleDto> updateVehicle(
			@Parameter(
					name = "id",
					description = "ID del vehículo a actualizar",
					example = "1",
					in = ParameterIn.PATH,
					required = true
			)
			@PathVariable Long id,
			@RequestBody VehicleDto vehicleDto
	);

	@Operation(
			summary = "Eliminar vehículo",
			description = "Elimina permanentemente un vehículo del sistema",
			security = @SecurityRequirement(name = "JWT"),
			responses = {
					@ApiResponse(
							responseCode = "204",
							description = "Vehículo eliminado exitosamente"
					),
					@ApiResponse(
							responseCode = "400",
							description = "ID inválido o vehículo no encontrado",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = @ExampleObject(
											value = "{\"message\": \"Vehículo no encontrado con ID: 999\", \"status\": 400}"
									)
							)
					),
					@ApiResponse(
							responseCode = "403",
							description = "Acceso denegado - Se requiere rol ADMIN",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class)
							)
					)
			}
	)
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Void> deleteVehicle(
			@Parameter(
					name = "id",
					description = "ID del vehículo a eliminar",
					example = "1",
					in = ParameterIn.PATH,
					required = true
			)
			@PathVariable Long id
	);
}