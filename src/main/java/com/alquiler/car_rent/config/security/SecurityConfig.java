package com.alquiler.car_rent.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs REST
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // Endpoints de autenticación
                .requestMatchers(
                    ApiPathConstants.V1_ROUTE + ApiPathConstants.AUTH_ROUTE + "/login",
                    ApiPathConstants.V1_ROUTE + ApiPathConstants.AUTH_ROUTE + "/register"
                ).permitAll()
                
                // Endpoint exclusivos para MANAGER
                .requestMatchers(ApiPathConstants.V1_ROUTE + "/users/**").hasRole("MANAGER")
                
                // Operaciones de escritura en customers y vehicles para MANAGER y ADMIN
                .requestMatchers(
                    HttpMethod.POST, 
                    ApiPathConstants.V1_ROUTE + "/customers/**", 
                    ApiPathConstants.V1_ROUTE + "/vehicles/**"
                ).hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(
                    HttpMethod.PUT, 
                    ApiPathConstants.V1_ROUTE + "/customers/**", 
                    ApiPathConstants.V1_ROUTE + "/vehicles/**"
                ).hasRole("MANAGER")
                .requestMatchers(
                    HttpMethod.DELETE, 
                    ApiPathConstants.V1_ROUTE + "/customers/**", 
                    ApiPathConstants.V1_ROUTE + "/vehicles/**"
                ).hasAnyRole("MANAGER", "ADMIN")
                
                // Endpoints compartidos entre MANAGER y ADMIN
                .requestMatchers(ApiPathConstants.V1_ROUTE + "/rentals/**").hasAnyRole("MANAGER", "ADMIN")
                
                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )
            // Añadir filtro JWT antes del filtro de autenticación de username/password
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}