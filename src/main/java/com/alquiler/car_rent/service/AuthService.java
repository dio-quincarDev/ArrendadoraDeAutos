package com.alquiler.car_rent.service;

import com.alquiler.car_rent.commons.dtos.LoginRequest;
import com.alquiler.car_rent.commons.dtos.TokenResponse;
import com.alquiler.car_rent.commons.dtos.UserEntityRequest;

public interface AuthService {
	
	TokenResponse createUser (UserEntityRequest userEntityRequest);
	
	TokenResponse login (LoginRequest loginRequest);

}
