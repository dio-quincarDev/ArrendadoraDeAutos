package com.alquiler.car_rent.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Schema;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@Schema(description = "Formato estándar para respuestas de error")
	public static class ErrorResponse {
		@Schema(description = "Mensaje descriptivo del error", example = "Recurso no encontrado")
		public String message;

		@Schema(description = "Código de estado HTTP", example = "404")
		public int status;

		// Constructor para Swagger
		public ErrorResponse() {}

		public ErrorResponse(String message, int status) {
			this.message = message;
			this.status = status;
		}
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		log.warn("Error de validación: {}", errors);
		return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
		Map<String, String> errors = ex.getConstraintViolations().stream()
				.collect(Collectors.toMap(
						violation -> violation.getPropertyPath().toString(),
						ConstraintViolation::getMessage
				));
		log.warn("Error de validación de entidad: {}", errors);
		return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(org.springframework.web.bind.MissingServletRequestParameterException ex, WebRequest request) {
		log.warn("Parámetro de solicitud faltante: {}", ex.getMessage());
		ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
		String error;
		Class<?> requiredType = ex.getRequiredType();
		if (requiredType != null && requiredType.isEnum()) {
			String validValues = Arrays.stream(requiredType.getEnumConstants())
					.map(Object::toString)
					.collect(Collectors.joining(", "));
			error = String.format("Valor '%s' inválido para el parámetro '%s'. Los valores permitidos son: [%s].", ex.getValue(), ex.getName(), validValues);
		} else {
			error = String.format("Parámetro '%s' debe ser de tipo '%s'.", ex.getName(), requiredType != null ? requiredType.getSimpleName() : "desconocido");
		}
		log.warn("Error de tipo de parámetro: {}", error);
		ErrorResponse errorResponse = new ErrorResponse(error, HttpStatus.BAD_REQUEST.value());
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex, WebRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
		ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value());
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
        log.warn("Solicitud incorrecta: {}", ex.getMessage());
		ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        // This is specifically for the manual login check in AuthServiceImpl
        if (ex.getMessage().contains("Usuario o contraseña inválidos")) {
            log.warn("Intento de login fallido: {}", ex.getMessage());
            ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED.value());
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }
        // For other illegal argument errors, return Bad Request
        log.warn("Argumento ilegal: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

	@ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex, WebRequest request) {
		log.warn("Acceso denegado: {}", ex.getMessage());
		ErrorResponse error = new ErrorResponse("No tienes permiso para acceder a este recurso.", HttpStatus.FORBIDDEN.value());
		return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Ha ocurrido un error inesperado", ex);
		ErrorResponse error = new ErrorResponse(
				"Ha ocurrido un error inesperado",
				HttpStatus.INTERNAL_SERVER_ERROR.value()
		);
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
