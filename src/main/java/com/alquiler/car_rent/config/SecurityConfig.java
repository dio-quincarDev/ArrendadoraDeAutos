package com.alquiler.car_rent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Definir constante para la ruta base de la API
    private static final String V1_ROUTE = "/v1";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/login"
                ).permitAll()
                
                // Endpoint exclusivos para MANAGER
                .requestMatchers(V1_ROUTE + "/users/**").hasRole("MANAGER")
                
                // Operaciones de escritura en customers y vehicles para MANAGER y ADMIN
                .requestMatchers(
                    HttpMethod.POST, 
                    V1_ROUTE + "/customers/**", 
                    V1_ROUTE + "/vehicles/**"
                ).hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(
                    HttpMethod.PUT, 
                    V1_ROUTE + "/customers/**", 
                    V1_ROUTE + "/vehicles/**"
                ).hasRole("MANAGER")
                .requestMatchers(
                    HttpMethod.DELETE, 
                    V1_ROUTE + "/customers/**", 
                    V1_ROUTE + "/vehicles/**"
                ).hasAnyRole("MANAGER", "ADMIN")
                
                // Endpoints compartidos entre MANAGER y ADMIN
                .requestMatchers(V1_ROUTE + "/rentals/**").hasAnyRole("MANAGER", "ADMIN")
                
                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .csrf(); // Habilita CSRF por defecto, puedes personalizar si es necesario

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}