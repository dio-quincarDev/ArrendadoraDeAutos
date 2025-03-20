package com.alquiler.car_rent.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;
import com.alquiler.car_rent.commons.dtos.TokenResponse;
import com.alquiler.car_rent.commons.dtos.UserEntityRequest;

import jakarta.validation.Valid;

@RequestMapping(ApiPathConstants.V1_ROUTE + ApiPathConstants.AUTH_ROUTE)
public interface AuthApi {
	
	
	    @PostMapping("/register")
	    ResponseEntity<TokenResponse> createUser (@RequestBody @Valid UserEntityRequest userEntityRequest);

	    @GetMapping
	    ResponseEntity<String> getUser(@RequestAttribute(name="X-User-Id") @Valid String userEnityId);


}
