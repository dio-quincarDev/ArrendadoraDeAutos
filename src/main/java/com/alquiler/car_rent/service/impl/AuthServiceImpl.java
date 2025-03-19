package com.alquiler.car_rent.service.impl;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.alquiler.car_rent.commons.dtos.LoginRequest;
import com.alquiler.car_rent.commons.dtos.TokenResponse;
import com.alquiler.car_rent.commons.dtos.UserEntityRequest;
import com.alquiler.car_rent.commons.entities.UserEntity;
import com.alquiler.car_rent.commons.enums.Role;
import com.alquiler.car_rent.repositories.UserEntityRepository;
import com.alquiler.car_rent.service.AuthService;
import com.alquiler.car_rent.service.JwtService;

@Service
public class AuthServiceImpl implements AuthService {
	
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
	public TokenResponse createUser(UserEntityRequest userEntityRequest) {
		return Optional.of(userEntityRequest)
				.map(this::mapToEntity)
				.map(userEntityRepository::save)
				.map(userCreated-> jwtService.generateToken(userCreated.getId()))
				.orElseThrow(()-> new RuntimeException("Error creando Usuario"));
	}

	@Override
	public TokenResponse login(LoginRequest loginRequest) {
		UserEntity user = userEntityRepository.findByEmail(loginRequest.getEmail())
				.orElseThrow(()-> new IllegalArgumentException("Usuario No Encontrado"));
		if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
			throw new IllegalArgumentException("Invalid Password ");
		}
		return jwtService.generateToken(user.getId());
	}
	
	private UserEntity mapToEntity (UserEntityRequest userEntityRequest) {
		return UserEntity.builder()
				.email(userEntityRequest.getEmail())
				.password(passwordEncoder.encode(userEntityRequest.getPassword()))
				.role(Role.ROLE_MANAGER)
				.build();
	}
	

}
