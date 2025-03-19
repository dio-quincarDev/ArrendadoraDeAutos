package com.alquiler.car_rent.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alquiler.car_rent.commons.dtos.TokenResponse;
import com.alquiler.car_rent.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService {
	
	private final SecretKey secretKey;
	private static final long EXPIRATION_TIME = 864000000;
	
	  public JwtServiceImpl(@Value("${jwt.secret}") String secret) {
	        if(secret.getBytes().length <32){
	            throw new IllegalArgumentException("Secret key must be at least 32 characters long.");
	        }
	        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	    }

	@Override
	public TokenResponse generateToken(Long userEntityId) {
		Date now = new Date();
		Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME);
		
		String token = Jwts.builder()
				.subject(String.valueOf(userEntityId))
				.claim("useEntityId", userEntityId)
				.issuedAt(now)
				.expiration(expirationDate)
				.signWith(secretKey, SignatureAlgorithm.HS256)
				.compact();
		
		return TokenResponse.builder()
				.accesToken(token)
				.build();
	}

	@Override
	public Claims getClaims(String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isExpired(String token) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Integer extractUserEntityId(String token) {
		// TODO Auto-generated method stub
		return null;
	}

}
