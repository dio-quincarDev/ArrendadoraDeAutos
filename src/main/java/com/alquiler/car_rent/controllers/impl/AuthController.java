package com.alquiler.car_rent.controllers.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.alquiler.car_rent.commons.dtos.LoginRequest;
import com.alquiler.car_rent.commons.dtos.TokenResponse;
import com.alquiler.car_rent.commons.dtos.UserEntityRequest;
import com.alquiler.car_rent.controllers.AuthApi;
import com.alquiler.car_rent.service.AuthService;

import jakarta.validation.Valid;

@RestController
public class AuthController implements AuthApi {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<TokenResponse> createUser(@RequestBody @Valid UserEntityRequest userEntityRequest) {
        TokenResponse response = authService.createUser(userEntityRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        TokenResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<String> getUser(@RequestAttribute(name = "X-User-Id") @Valid String userEntityId) {
        return ResponseEntity.ok(userEntityId);
    }
}