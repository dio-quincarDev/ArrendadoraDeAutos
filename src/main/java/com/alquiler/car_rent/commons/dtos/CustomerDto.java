package com.alquiler.car_rent.commons.dtos;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para la transferencia de datos de clientes")
public record CustomerDto(
        @Schema(description = "ID único del cliente", example = "1")
        Long id, 
        @Schema(description = "Nombre completo del cliente", example = "Juan Pérez")
        String name, 
        @Schema(description = "Dirección de correo electrónico del cliente", example = "juan.perez@example.com")
        String email, 
        @Schema(description = "Número de licencia de conducir del cliente", example = "ABC12345")
        String license,
        @Schema(description = "Fecha y hora de creación del registro del cliente", example = "2023-07-21T10:00:00")
        LocalDateTime createdAt,
        @Schema(description = "Fecha y hora de la última actualización del registro del cliente", example = "2023-07-21T11:30:00")
        LocalDateTime updatedAt,
        @Schema(description = "Número de teléfono del cliente", example = "+50761234567")
        String phone,
        @Schema(description = "Estado actual del cliente (ej. ACTIVO, INACTIVO)", example = "ACTIVO")
        String customerStatus) {
}

