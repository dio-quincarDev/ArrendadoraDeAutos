package com.alquiler.car_rent.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
		name = "SMS Service",
		description = "Endpoints para el envío de mensajes SMS a clientes"
)
@RequestMapping("/v1/sms")
public interface SmsApi {

	@Operation(
			summary = "Enviar SMS",
			description = "Envía un mensaje SMS a un número de teléfono específico usando Twilio API",
			security = @SecurityRequirement(name = "JWT"),
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "SMS enviado exitosamente",
							content = @Content(
									mediaType = "text/plain",
									examples = @ExampleObject(
											value = "SMS enviado con éxito a +50761234567"
									)
							)
					),
					@ApiResponse(
							responseCode = "400",
							description = "Número de teléfono inválido o mensaje vacío",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = @ExampleObject(
											value = "{\"message\": \"Número de teléfono inválido\", \"status\": 400}"
									)
							)
					),
					@ApiResponse(
							responseCode = "500",
							description = "Error al enviar SMS (problema con Twilio API)",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = ErrorResponse.class),
									examples = @ExampleObject(
											value = "{\"message\": \"Error al enviar SMS: Credenciales inválidas\", \"status\": 500}"
									)
							)
					)
			}
	)
	@PostMapping("/send")
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<String> sendSms(
			@Parameter(
					name = "to",
					description = "Número de teléfono destino en formato E.164",
					example = "+50761234567",
					required = true,
					in = ParameterIn.QUERY
			)
			@RequestParam String to,

			@Parameter(
					name = "message",
					description = "Contenido del mensaje SMS (máx. 160 caracteres)",
					example = "Su vehículo estará listo para recoger a las 14:00",
					required = true,
					in = ParameterIn.QUERY
			)
			@RequestParam String message
	);
}