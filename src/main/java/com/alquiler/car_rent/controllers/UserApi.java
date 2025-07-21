package com.alquiler.car_rent.controllers;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;
import com.alquiler.car_rent.commons.dtos.UserEntityRequest;
import com.alquiler.car_rent.commons.entities.UserEntity;
import com.alquiler.car_rent.exceptions.GlobalExceptionHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.alquiler.car_rent.commons.constants.ApiPathConstants.V1_ROUTE;
import static com.alquiler.car_rent.commons.constants.ApiPathConstants.USERS_BASE_PATH;

@Tag(name = "User Management", description = "API para la gestión de usuarios")
@RequestMapping(ApiPathConstants.V1_ROUTE + ApiPathConstants.USERS_BASE_PATH)
public interface UserApi {

    @Operation(
            summary = "Obtener todos los usuarios",
            description = "Recupera una lista de todos los usuarios registrados en el sistema. Requiere rol ADMIN.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)")
            }
    )
    @GetMapping
    ResponseEntity<List<UserEntity>> getAllUsers();

    @Operation(
            summary = "Obtener usuario por ID",
            description = "Recupera los detalles de un usuario específico por su ID. Requiere rol ADMIN.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario obtenido exitosamente"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Usuario no encontrado",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"message\": \"Usuario no encontrado con ID: 999\", \"status\": 404}")
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)")
            }
    )
    @GetMapping("/{id}")
    ResponseEntity<UserEntity> getUserById(
            @Parameter(
                    name = "id",
                    description = "ID único del usuario",
                    example = "1",
                    in = ParameterIn.PATH,
                    required = true
            )
            @PathVariable Long id);

    @Operation(
            summary = "Crear un nuevo usuario",
            description = "Registra un nuevo usuario en el sistema. Requiere rol ADMIN.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solicitud inválida",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"message\": \"El email ya está registrado\", \"status\": 400}")
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)")
            }
    )
    @PostMapping
    ResponseEntity<UserEntity> createUser(@RequestBody UserEntityRequest userRequest);

    @Operation(
            summary = "Actualizar un usuario existente",
            description = "Modifica los datos de un usuario existente por su ID. Requiere rol ADMIN.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Usuario no encontrado",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"message\": \"Usuario no encontrado con ID: 999\", \"status\": 404}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solicitud inválida (ej. email duplicado)",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"message\": \"El email ya está registrado\", \"status\": 400}")
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)")
            }
    )
    @PutMapping("/{id}")
    ResponseEntity<UserEntity> updateUser(
            @Parameter(
                    name = "id",
                    description = "ID del usuario a actualizar",
                    example = "1",
                    in = ParameterIn.PATH,
                    required = true
            )
            @PathVariable Long id,
            @RequestBody UserEntityRequest userRequest);

    @Operation(
            summary = "Eliminar un usuario",
            description = "Elimina permanentemente un usuario del sistema por su ID. Requiere rol ADMIN.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Usuario no encontrado",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"message\": \"Usuario no encontrado con ID: 999\", \"status\": 404}")
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)")
            }
    )
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(
            @Parameter(
                    name = "id",
                    description = "ID del usuario a eliminar",
                    example = "1",
                    in = ParameterIn.PATH,
                    required = true
            )
            @PathVariable Long id);

    @Operation(
            summary = "Actualizar el rol de un usuario",
            description = "Modifica el rol de un usuario existente. Requiere rol ADMIN.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Rol de usuario actualizado exitosamente"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Usuario no encontrado",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"message\": \"Usuario no encontrado con ID: 999\", \"status\": 404}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solicitud inválida (ej. rol no válido)",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"message\": \"Rol no válido: INVALID_ROLE\", \"status\": 400}")
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)")
            }
    )
    @PutMapping("/{id}/role")
    ResponseEntity<UserEntity> updateUserRole(
            @Parameter(
                    name = "id",
                    description = "ID del usuario a actualizar",
                    example = "1",
                    in = ParameterIn.PATH,
                    required = true
            )
            @PathVariable Long id,
            @Parameter(
                    name = "newRole",
                    description = "Nuevo rol para el usuario (ADMIN, USERS)",
                    example = "USERS",
                    in = ParameterIn.QUERY,
                    required = true
            )
            @RequestParam String newRole);
}
