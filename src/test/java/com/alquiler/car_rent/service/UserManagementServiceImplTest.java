package com.alquiler.car_rent.service;

import com.alquiler.car_rent.commons.dtos.UserEntityRequest;
import com.alquiler.car_rent.commons.entities.UserEntity;
import com.alquiler.car_rent.commons.enums.Role;
import com.alquiler.car_rent.exceptions.BadRequestException;
import com.alquiler.car_rent.repositories.UserEntityRepository;
import com.alquiler.car_rent.service.impl.UserManagementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserManagementServiceImplTest {

    @Mock
    private UserEntityRepository userEntityRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Environment env;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserManagementServiceImpl userManagementService;

    private UserEntityRequest userRequest;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userRequest = new UserEntityRequest("newuser@example.com", "newPassword", Role.USERS, "newUsername");
        // Corregido el orden de los argumentos del constructor para que coincida con la entidad
        userEntity = new UserEntity(1L, "test@example.com", "testuser", "encodedPass", Role.USERS, true, true, true, true);
    }

    // --- Métodos de ayuda para configurar el contexto de seguridad ---

    private void setupSecurityContextForUser(String username) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
    }

    private void setupSecurityContextWithRole(String role) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getAuthorities()).thenReturn(Collections.singletonList(new SimpleGrantedAuthority(role)));
    }

    // --- Pruebas para createUser ---

    @Test
    void createUser_shouldSucceed_withValidData() {
        when(userEntityRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userEntityRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userEntityRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity createdUser = userManagementService.createUser(userRequest);

        assertNotNull(createdUser);
        assertEquals(userRequest.getUsername(), createdUser.getUsername());
        verify(userEntityRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void createUser_shouldThrowBadRequest_whenEmailExists() {
        when(userEntityRepository.findByEmail(userRequest.getEmail())).thenReturn(Optional.of(new UserEntity()));
        assertThrows(BadRequestException.class, () -> userManagementService.createUser(userRequest));
    }

    // --- Pruebas para updateUser ---

    @Test
    void updateUser_shouldUpdateUsernameAndEmail_butNotPassword() {
        when(userEntityRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userEntityRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity updatedUser = userManagementService.updateUser(1L, userRequest);

        assertEquals("newUsername", updatedUser.getUsername());
        assertEquals("newuser@example.com", updatedUser.getEmail());
        assertEquals("encodedPass", updatedUser.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userEntityRepository, times(1)).save(userEntity);
    }

    @Test
    void updateUser_shouldThrowBadRequest_whenEmailIsInUse() {
        when(userEntityRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userEntityRepository.findByEmail(userRequest.getEmail())).thenReturn(Optional.of(new UserEntity()));
        assertThrows(BadRequestException.class, () -> userManagementService.updateUser(1L, userRequest));
    }

    // --- Pruebas para deleteUser ---

    @Test
    void deleteUser_shouldSucceed_whenDeletedByInitialAdmin() {
        setupSecurityContextForUser("initialAdmin");
        when(env.getProperty("application.initial-admin.username")).thenReturn("initialAdmin");
        when(userEntityRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        userManagementService.deleteUser(1L);

        verify(userEntityRepository, times(1)).delete(userEntity);
    }

    @Test
    void deleteUser_shouldThrowBadRequest_whenInitialAdminDeletesHimself() {
        setupSecurityContextForUser("initialAdmin");
        when(env.getProperty("application.initial-admin.username")).thenReturn("initialAdmin");
        userEntity.setUsername("initialAdmin");
        when(userEntityRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        assertThrows(BadRequestException.class, () -> userManagementService.deleteUser(1L));
    }

    @Test
    void deleteUser_shouldThrowBadRequest_whenDeletedByNonInitialAdmin() {
        setupSecurityContextForUser("anotherAdmin");
        when(env.getProperty("application.initial-admin.username")).thenReturn("initialAdmin");
        when(userEntityRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        assertThrows(BadRequestException.class, () -> userManagementService.deleteUser(1L));
    }

    // --- Pruebas para updateUserRole ---

    @Test
    void updateUserRole_shouldSucceed_whenUpdatedBySuperAdmin() {
        setupSecurityContextWithRole("ROLE_SUPER_ADMIN");
        when(userEntityRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userEntityRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        userManagementService.updateUserRole(1L, Role.ADMIN);

        assertEquals(Role.ADMIN, userEntity.getRole());
        verify(userEntityRepository, times(1)).save(userEntity);
    }

    @Test
    void updateUserRole_shouldThrowBadRequest_whenUpdatedByNonSuperAdmin() {
        setupSecurityContextWithRole("ROLE_ADMIN");
        when(userEntityRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        assertThrows(BadRequestException.class, () -> userManagementService.updateUserRole(1L, Role.ADMIN));
    }
}