package com.alquiler.car_rent.service.impl;

import java.util.Optional;
import java.util.Objects; // Importar Objects para chequeo de null

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alquiler.car_rent.commons.dtos.LoginRequest;
import com.alquiler.car_rent.commons.dtos.TokenResponse;
import com.alquiler.car_rent.commons.dtos.UserEntityRequest; // DTO
import com.alquiler.car_rent.commons.entities.UserEntity; // Entidad
import com.alquiler.car_rent.commons.enums.Role; // Enum
import com.alquiler.car_rent.repositories.UserEntityRepository;
import com.alquiler.car_rent.service.AuthService;
import com.alquiler.car_rent.service.JwtService;

import jakarta.validation.Valid; // Importar para validar el DTO si es necesario en el controller

@Service
public class AuthServiceImpl implements AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

	private final UserEntityRepository userEntityRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthServiceImpl(UserEntityRepository userEntityRepository, PasswordEncoder passwordEncoder,
						   JwtService jwtService) {
		this.userEntityRepository = userEntityRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Override
	// Considera añadir @Valid en el Controller para que se apliquen las validaciones del DTO
	public TokenResponse createUser(@Valid UserEntityRequest userEntityRequest) {
		log.info("Intentando crear usuario para email: {}", userEntityRequest.getEmail());
		// Las validaciones @NotBlank, @Email, @NotNull del DTO se deberían chequear antes
		// de llamar a este método (usualmente en la capa del Controller con @Valid).
		// Aquí podemos añadir validaciones de lógica de negocio si es necesario.

		// Verificar si el usuario ya existe
		if (userEntityRepository.findByEmail(userEntityRequest.getEmail()).isPresent()) {
			log.warn("Intento de crear usuario con email existente: {}", userEntityRequest.getEmail());
			throw new IllegalArgumentException("El email ya está registrado.");
		}

		try {
			// El mapeo ahora es más directo gracias a que el DTO usa Role
			UserEntity userToSave = mapToEntity(userEntityRequest);

			UserEntity userCreated = userEntityRepository.save(userToSave);
			log.info("Usuario creado exitosamente con ID: {}", userCreated.getId());

			// Obtener el nombre del rol para el token JWT
			// Asumimos que userCreated.getRole() no será null debido a la lógica en mapToEntity
			// y la validación @NotNull en el DTO (si se usa @Valid en el controller).
			String roleName = userCreated.getRole().name();
			return jwtService.generateToken(userCreated.getId(), roleName);

		} catch (Exception e) {
			log.error("Error durante la creación del usuario para email {}: {}", userEntityRequest.getEmail(), e.getMessage(), e);
			// Considera lanzar una excepción más específica si es posible
			throw new RuntimeException("Error interno al crear el usuario.", e);
		}
	}

	@Override
	public TokenResponse login(LoginRequest loginRequest) {
		log.info("Intentando login para usuario: {}", loginRequest.getEmail());
		UserEntity user = userEntityRepository.findByEmail(loginRequest.getEmail())
				.orElseThrow(()-> {
					log.warn("Intento de login fallido - Usuario no encontrado: {}", loginRequest.getEmail());
					return new IllegalArgumentException("Usuario o contraseña inválidos.");
				});

		if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
			log.warn("Intento de login fallido - Contraseña inválida para usuario: {}", loginRequest.getEmail());
			throw new IllegalArgumentException("Usuario o contraseña inválidos.");
		}

		// Asumimos que getRole() no será null para un usuario existente válido.
		String roleName = user.getRole().name();
		log.info("Login exitoso para usuario: {}", loginRequest.getEmail());
		return jwtService.generateToken(user.getId(), roleName);
	}

	// *** MÉTODO CORREGIDO ***
	private UserEntity mapToEntity(UserEntityRequest userEntityRequest) {
		// Obtener el rol directamente del DTO (es de tipo Role)
		Role requestedRole = userEntityRequest.getRole();

		// Si el rol del DTO es null (aunque @NotNull debería prevenirlo si se usa @Valid),
		// o si quieres forzar un rol por defecto en caso de lógica adicional,
		// podrías asignar Role.USER aquí. Pero confiando en @NotNull:
		if (requestedRole == null) {
			// Esto no debería ocurrir si @Valid se usa en el controller y @NotNull está en el DTO.
			log.error("El rol llegó nulo a mapToEntity a pesar de @NotNull para el usuario {}", userEntityRequest.getEmail());
			// Decide cómo manejarlo: lanzar excepción o asignar defecto.
			// throw new IllegalStateException("El rol no puede ser nulo en este punto.");
			requestedRole = Role.USERS; // Asignar defecto como fallback MUY seguro.
			// Si 'Cannot resolve symbol USER' persiste, esta línea fallará.
		}

		log.debug("Mapeando DTO a Entidad con Rol: {}", requestedRole);

		// Construir la entidad pasando el objeto Role directamente.
		return UserEntity.builder()
				.email(userEntityRequest.getEmail())
				.password(passwordEncoder.encode(userEntityRequest.getPassword()))
				.role(requestedRole) // Pasamos el objeto Role obtenido del DTO (o el defecto)
				.username(userEntityRequest.getUsername())
				.build();
	}
}
