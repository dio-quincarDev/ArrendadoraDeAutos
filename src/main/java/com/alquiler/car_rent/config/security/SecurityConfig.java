package com.alquiler.car_rent.config.security;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain publicAndWebSocketFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/admin-alerts/**"
            )
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(ApiPathConstants.V1_ROUTE + "/**")
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos de la API
                .requestMatchers(ApiPathConstants.V1_ROUTE + ApiPathConstants.AUTH_ROUTE + "/login").permitAll()
                .requestMatchers(ApiPathConstants.V1_ROUTE + ApiPathConstants.AUTH_ROUTE + "/register").permitAll()
                .requestMatchers(HttpMethod.GET, ApiPathConstants.V1_ROUTE + ApiPathConstants.VEHICLE_ROUTE + "/**").permitAll()

                // Permitir OPTIONS para la ruta de reportes (para CORS preflight)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Endpoints exclusivos para ADMIN
                .requestMatchers(ApiPathConstants.V1_ROUTE + ApiPathConstants.USERS_BASE_PATH + "/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(ApiPathConstants.V1_ROUTE + ApiPathConstants.REPORTS_BASE_PATH + "/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(ApiPathConstants.V1_ROUTE + ApiPathConstants.SMS_ROUTE + "/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, ApiPathConstants.V1_ROUTE + ApiPathConstants.CUSTOMER_ROUTE + "/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, ApiPathConstants.V1_ROUTE + ApiPathConstants.CUSTOMER_ROUTE + "/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, ApiPathConstants.V1_ROUTE + ApiPathConstants.VEHICLE_ROUTE + "/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, ApiPathConstants.V1_ROUTE + ApiPathConstants.VEHICLE_ROUTE + "/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, ApiPathConstants.V1_ROUTE + ApiPathConstants.RENTAL_ROUTE + "/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                // Endpoints para USERS y ADMIN
                .requestMatchers(ApiPathConstants.V1_ROUTE + ApiPathConstants.CUSTOMER_ROUTE + "/**").hasAnyRole("USERS", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(ApiPathConstants.V1_ROUTE + ApiPathConstants.RENTAL_ROUTE + "/**").hasAnyRole("USERS", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(ApiPathConstants.V1_ROUTE + ApiPathConstants.VEHICLE_ROUTE + "/**").hasAnyRole("USERS", "ADMIN", "SUPER_ADMIN")

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
