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
                        .requestMatchers(
                                ApiPathConstants.V1_ROUTE + ApiPathConstants.AUTH_ROUTE + "/login",
                                ApiPathConstants.V1_ROUTE + ApiPathConstants.AUTH_ROUTE + "/register"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, ApiPathConstants.V1_ROUTE + ApiPathConstants.VEHICLE_ROUTE + "/**").permitAll()

                        // Permitir OPTIONS para la ruta de reportes (para CORS preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Endpoints exclusivos para ADMIN
                        .requestMatchers(ApiPathConstants.V1_ROUTE + ApiPathConstants.USERS_BASE_PATH + "/**").hasRole("ADMIN")
                        .requestMatchers(ApiPathConstants.V1_ROUTE + ApiPathConstants.REPORTS_BASE_PATH + "/**").hasRole("ADMIN")
                        .requestMatchers(ApiPathConstants.V1_ROUTE + ApiPathConstants.SMS_ROUTE + "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, ApiPathConstants.V1_ROUTE + ApiPathConstants.CUSTOMER_ROUTE + "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, ApiPathConstants.V1_ROUTE + ApiPathConstants.CUSTOMER_ROUTE + "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, ApiPathConstants.V1_ROUTE + ApiPathConstants.VEHICLE_ROUTE + "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, ApiPathConstants.V1_ROUTE + ApiPathConstants.VEHICLE_ROUTE + "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, ApiPathConstants.V1_ROUTE + ApiPathConstants.RENTAL_ROUTE + "/**").hasRole("ADMIN")

                        // Endpoints para USERS y ADMIN
                        .requestMatchers(ApiPathConstants.V1_ROUTE + ApiPathConstants.CUSTOMER_ROUTE + "/**").hasAnyRole("USERS", "ADMIN")
                        .requestMatchers(ApiPathConstants.V1_ROUTE + ApiPathConstants.RENTAL_ROUTE + "/**").hasAnyRole("USERS", "ADMIN")
                        .requestMatchers(ApiPathConstants.V1_ROUTE + ApiPathConstants.VEHICLE_ROUTE + "/**").hasAnyRole("USERS", "ADMIN")

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