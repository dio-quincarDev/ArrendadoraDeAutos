package com.alquiler.car_rent.controllers.impl;

import com.alquiler.car_rent.commons.dtos.UserEntityRequest;
import com.alquiler.car_rent.commons.entities.UserEntity;
import com.alquiler.car_rent.commons.enums.Role;
import com.alquiler.car_rent.controllers.UserApi;
import com.alquiler.car_rent.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserManagementService userManagementService;

    @Override
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        return ResponseEntity.ok(userManagementService.getAllUsers());
    }

    @Override
    public ResponseEntity<UserEntity> getUserById(Long id) {
        return ResponseEntity.ok(userManagementService.getUserById(id));
    }

    @Override
    public ResponseEntity<UserEntity> createUser(UserEntityRequest userRequest) {
        UserEntity newUser = userManagementService.createUser(userRequest);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<UserEntity> updateUser(Long id, UserEntityRequest userRequest) {
        UserEntity updatedUser = userManagementService.updateUser(id, userRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UserEntity> updateUserRole(Long id, String newRole) {
        Role roleEnum;
        try {
            roleEnum = Role.valueOf(newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol inválido: " + newRole + ". Los roles permitidos son: " + java.util.Arrays.toString(Role.values()));
        }
        UserEntity updatedUser = userManagementService.updateUserRole(id, roleEnum);
        return ResponseEntity.ok(updatedUser);
    }
}
