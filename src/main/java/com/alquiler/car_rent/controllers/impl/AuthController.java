package com.alquiler.car_rent.controllers.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;
import com.alquiler.car_rent.commons.dtos.TokenResponse;
import com.alquiler.car_rent.commons.dtos.UserEntityRequest;
import com.alquiler.car_rent.controllers.AuthApi;
import com.alquiler.car_rent.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPathConstants.V1_ROUTE + ApiPathConstants.AUTH_ROUTE + ApiPathConstants.LOGIN_ROUTE)
public class AuthController implements AuthApi {
	private final AuthService authService;
	
	  public AuthController(AuthService authService) {
	        this.authService = authService;
	    }

	@Override
	public ResponseEntity<TokenResponse> createUser(@Valid UserEntityRequest userEntityRequest) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<String> getUser(@Valid String userEnityId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
