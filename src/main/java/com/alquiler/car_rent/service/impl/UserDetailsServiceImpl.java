package com.alquiler.car_rent.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.alquiler.car_rent.repositories.UserEntityRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
		private final UserEntityRepository userEntityRepository;
		
		   public UserDetailsServiceImpl(UserEntityRepository userEntityRepository) {
		        this.userEntityRepository = userEntityRepository;
		    }
		
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		return userEntityRepository.findByEmail(username)
				.orElseThrow(()-> new UsernameNotFoundException("User with Email" + username + "not found"));
	}

}
