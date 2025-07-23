package com.alquiler.car_rent.service;

import com.alquiler.car_rent.commons.dtos.UserEntityRequest;
import com.alquiler.car_rent.commons.entities.UserEntity;
import com.alquiler.car_rent.commons.enums.Role;

import java.util.List;

public interface UserManagementService {
    List<UserEntity> getAllUsers();
    UserEntity getUserById(Long id);
    UserEntity createUser(UserEntityRequest userRequest);
    UserEntity updateUser(Long id, UserEntityRequest userRequest);
    void deleteUser(Long id);
    UserEntity updateUserRole(Long id, Role newRole);
}
