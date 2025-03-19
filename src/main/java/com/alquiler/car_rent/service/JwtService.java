package com.alquiler.car_rent.service;

import com.alquiler.car_rent.commons.dtos.TokenResponse;

import io.jsonwebtoken.Claims;

public interface JwtService {
	TokenResponse generateToken (Long userEntityId);
	Claims getClaims(String token);
	boolean isExpired(String token);
	Integer extractUserEntityId(String token);
	

}
