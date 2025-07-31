package com.alquiler.car_rent.service;

import com.alquiler.car_rent.commons.dtos.LoginRequest;
import com.alquiler.car_rent.commons.dtos.TokenResponse;
import com.alquiler.car_rent.commons.dtos.UserEntityRequest;
import com.alquiler.car_rent.commons.entities.UserEntity;
import com.alquiler.car_rent.commons.enums.Role;
import com.alquiler.car_rent.repositories.UserEntityRepository;
import com.alquiler.car_rent.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserEntityRepository userEntityRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserEntityRequest userEntityRequest;
    private UserEntity userEntity;
    private LoginRequest loginRequest;
    private TokenResponse tokenResponse;

    @BeforeEach
    void setUp() {
        // Configuración para la creación de usuario
        userEntityRequest = UserEntityRequest.builder()
                .email("test@example.com")
                .password("password123")
                .username("testuser")
                .role(Role.USERS)
                .build();

        // Configuración de la entidad de usuario
        userEntity = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.USERS)
                .build();

        // Configuración para el login
        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        // Configuración de la respuesta del token
        tokenResponse = TokenResponse.builder()
                .accesToken("mocked-jwt-token")
                .build();
    }

    // Caso de éxito: Crear un usuario cuando el email no existe.
    @Test
    void createUser_shouldSucceed_whenEmailIsNew() {
        // Arrange
        when(userEntityRepository.findByEmail(userEntityRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userEntityRequest.getPassword())).thenReturn("encodedPassword");
        when(userEntityRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(jwtService.generateToken(userEntity.getId(), userEntity.getRole().name())).thenReturn(tokenResponse);

        // Act
        TokenResponse result = authService.createUser(userEntityRequest);

        // Assert
        assertNotNull(result);
        assertEquals("mocked-jwt-token", result.getAccesToken());
    }

    // Caso borde: Intentar crear un usuario con un email que ya está registrado.
    @Test
    void createUser_shouldThrowIllegalArgumentException_whenEmailExists() {
        // Arrange
        when(userEntityRepository.findByEmail(userEntityRequest.getEmail())).thenReturn(Optional.of(userEntity));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.createUser(userEntityRequest);
        });
        assertEquals("El email ya está registrado.", exception.getMessage());
    }

    // Caso de éxito: Login con credenciales correctas.
    @Test
    void login_shouldSucceed_whenCredentialsAreCorrect() {
        // Arrange
        when(userEntityRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(loginRequest.getPassword(), userEntity.getPassword())).thenReturn(true);
        when(jwtService.generateToken(userEntity.getId(), userEntity.getRole().name())).thenReturn(tokenResponse);

        // Act
        TokenResponse result = authService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("mocked-jwt-token", result.getAccesToken());
    }

    // Caso borde: Login con un email que no está registrado.
    @Test
    void login_shouldThrowIllegalArgumentException_whenUserNotFound() {
        // Arrange
        when(userEntityRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("Usuario o contraseña inválidos.", exception.getMessage());
    }

    // Caso borde: Login con contraseña incorrecta.
    @Test
    void login_shouldThrowIllegalArgumentException_whenPasswordIsIncorrect() {
        // Arrange
        when(userEntityRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(loginRequest.getPassword(), userEntity.getPassword())).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("Usuario o contraseña inválidos.", exception.getMessage());
    }
}
