package com.alquiler.car_rent.controllers;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;
import com.alquiler.car_rent.commons.dtos.UserEntityRequest;
import com.alquiler.car_rent.commons.entities.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.alquiler.car_rent.commons.constants.ApiPathConstants.V1_ROUTE;
import static com.alquiler.car_rent.commons.constants.ApiPathConstants.USERS_BASE_PATH;

@Tag(name = "User Management", description = "API para la gestión de usuarios")
@RequestMapping(ApiPathConstants.V1_ROUTE + ApiPathConstants.USERS_BASE_PATH)
public interface UserApi {

    @Operation(summary = "Obtener todos los usuarios", responses = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @GetMapping
    ResponseEntity<List<UserEntity>> getAllUsers();

    @Operation(summary = "Obtener usuario por ID", responses = {
            @ApiResponse(responseCode = "200", description = "Usuario obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @GetMapping("/{id}")
    ResponseEntity<UserEntity> getUserById(@PathVariable Long id);

    @Operation(summary = "Crear un nuevo usuario", responses = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @PostMapping
    ResponseEntity<UserEntity> createUser(@RequestBody UserEntityRequest userRequest);

    @Operation(summary = "Actualizar un usuario existente", responses = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @PutMapping("/{id}")
    ResponseEntity<UserEntity> updateUser(@PathVariable Long id, @RequestBody UserEntityRequest userRequest);

    @Operation(summary = "Eliminar un usuario", responses = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable Long id);

    @Operation(summary = "Actualizar el rol de un usuario", responses = {
            @ApiResponse(responseCode = "200", description = "Rol de usuario actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @PutMapping("/{id}/role")
    ResponseEntity<UserEntity> updateUserRole(@PathVariable Long id, @RequestParam String newRole);
}
