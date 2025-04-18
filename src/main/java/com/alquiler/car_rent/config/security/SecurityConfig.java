package com.alquiler.car_rent.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
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

                        // Permitir OPTIONS para la ruta de reportes (para CORS preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // Permite todas las peticiones OPTIONS

                        // Endpoints exclusivos para ADMIN
                        .requestMatchers(ApiPathConstants.V1_ROUTE + "/users/**").hasRole("ADMIN")
                        .requestMatchers(ApiPathConstants.V1_ROUTE + ApiPathConstants.REPORTS_BASE_PATH + "/reports").hasRole("ADMIN")
                        .requestMatchers(
                        	    HttpMethod.GET, 
                        	    ApiPathConstants.V1_ROUTE + ApiPathConstants.REPORTS_BASE_PATH + "/metrics/**"
                        	).hasRole("ADMIN")
                        	.requestMatchers(
                        	    HttpMethod.GET,
                        	    ApiPathConstants.V1_ROUTE + ApiPathConstants.REPORTS_BASE_PATH + "/export"
                        	).hasRole("ADMIN")
                        

                        // Operaciones sobre customers y vehicles - USER puede modificar (GET, PUT)
                        .requestMatchers(
                                HttpMethod.GET,
                                ApiPathConstants.V1_ROUTE + "/customers/**",
                                ApiPathConstants.V1_ROUTE + "/vehicles/**"
                        ).hasAnyRole("USERS", "ADMIN")
                        .requestMatchers(
                                HttpMethod.PUT,
                                ApiPathConstants.V1_ROUTE + "/customers/**",
                                ApiPathConstants.V1_ROUTE + "/vehicles/**"
                        ).hasAnyRole("USERS", "ADMIN")
                        // Operaciones sobre customers y vehicles - ADMIN puede crear y eliminar
                        .requestMatchers(
                                HttpMethod.POST,
                                ApiPathConstants.V1_ROUTE + "/customers/**",
                                ApiPathConstants.V1_ROUTE + "/vehicles/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.DELETE,
                                ApiPathConstants.V1_ROUTE + "/customers/**",
                                ApiPathConstants.V1_ROUTE + "/vehicles/**"
                        ).hasRole("ADMIN")

                        // Endpoints para rentals
                        .requestMatchers(ApiPathConstants.V1_ROUTE + "/rentals/**").hasAnyRole("USERS", "ADMIN")

                        // Cualquier otra solicitud requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}