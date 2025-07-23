package com.alquiler.car_rent.service.impl;

import com.alquiler.car_rent.commons.dtos.UserEntityRequest;
import com.alquiler.car_rent.commons.entities.UserEntity;
import com.alquiler.car_rent.commons.enums.Role;
import com.alquiler.car_rent.exceptions.BadRequestException;
import com.alquiler.car_rent.exceptions.NotFoundException;
import com.alquiler.car_rent.repositories.UserEntityRepository;
import com.alquiler.car_rent.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {
    private final UserEntityRepository userEntityRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Override
    public List<UserEntity> getAllUsers() {
        return userEntityRepository.findAll();
    }

    @Override
    public UserEntity getUserById(Long id) {
        return userEntityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + id));
    }

    @Override
    public UserEntity createUser(UserEntityRequest userRequest) {
        if (userEntityRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new BadRequestException("El email ya está registrado.");
        }
        if (userEntityRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            throw new BadRequestException("El nombre de usuario ya está en uso.");
        }

        UserEntity newUser = UserEntity.builder()
                .username(userRequest.getUsername())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .email(userRequest.getEmail())
                .role(userRequest.getRole() != null ? userRequest.getRole() : Role.USERS) // Default to USERS
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        return userEntityRepository.save(newUser);
    }

    @Override
    public UserEntity updateUser(Long id, UserEntityRequest userRequest) {
        UserEntity existingUser = getUserById(id);

        // Check for email/username uniqueness if they are being changed
        if (!existingUser.getEmail().equals(userRequest.getEmail()) && userEntityRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new BadRequestException("El email ya está registrado.");
        }
        if (!existingUser.getUsername().equals(userRequest.getUsername()) && userEntityRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            throw new BadRequestException("El nombre de usuario ya está en uso.");
        }

        existingUser.setUsername(userRequest.getUsername());
        existingUser.setEmail(userRequest.getEmail());
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }
        // Role update is handled by updateUserRole method

        return userEntityRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        UserEntity userToDelete = getUserById(id);
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        String initialAdminUsername = env.getProperty("application.initial-admin.username");

        // Solo el administrador inicial puede eliminar usuarios
        if (!authenticatedUsername.equals(initialAdminUsername)) {
            throw new BadRequestException("Solo el administrador inicial puede eliminar usuarios.");
        }

        // El administrador inicial no puede eliminarse a sí mismo
        if (userToDelete.getUsername().equals(initialAdminUsername)) {
            throw new BadRequestException("El administrador inicial no puede eliminarse a sí mismo.");
        }

        userEntityRepository.delete(userToDelete);
    }

    @Override
    @Transactional
    public UserEntity updateUserRole(Long id, Role newRole) {
        UserEntity userToUpdate = getUserById(id);
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        String initialAdminUsername = env.getProperty("application.initial-admin.username");

        // Logic to prevent non-initial admins from promoting/demoting other admins
        if (newRole == Role.ADMIN || userToUpdate.getRole() == Role.ADMIN) {
            if (!authenticatedUsername.equals(initialAdminUsername)) {
                throw new BadRequestException("Solo el administrador inicial puede modificar roles de administrador.");
            }
        }

        userToUpdate.setRole(newRole);
        return userEntityRepository.save(userToUpdate);
    }
}
