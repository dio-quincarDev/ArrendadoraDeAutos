package com.alquiler.car_rent.config.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional; // Import necesario para Optional

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.alquiler.car_rent.commons.entities.UserEntity; // Import necesario para UserEntity
import com.alquiler.car_rent.repositories.UserEntityRepository; // Import necesario
import com.alquiler.car_rent.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    // Inyectar el repositorio para la validación en BD
    private final UserEntityRepository userRepository;

    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserEntityRepository userRepository, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        Integer userId = null;
        String roleName = null;

        // 1. Verificar si hay token y si es Bearer
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.trace("No JWT token found in Authorization header or header does not start with Bearer.");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7); // Extraer el token

        try {
            // 2. Validar si el token ha expirado
            if (jwtService.isExpired(jwt)) {
                log.warn("JWT token has expired.");
                filterChain.doFilter(request, response);
                return;
            }

            // 3. Extraer información del token
            userId = jwtService.extractUserEntityId(jwt);
            roleName = jwtService.extractRole(jwt); // Esperamos "ADMIN", "USER", etc.

            log.debug("Token validated. UserID: {}, Role: {}", userId, roleName);

        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage(), e); // Loggear la excepción completa
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Si tenemos userId, rol y no hay autenticación previa en el contexto
        if (userId != null && StringUtils.hasText(roleName) && SecurityContextHolder.getContext().getAuthentication() == null) {

            // ---- Validación OBLIGATORIA en BD ----
            // Primero, verificar si el usuario existe y está activo/habilitado usando el repositorio
            Optional<UserEntity> userOptional = userRepository.findById(userId.longValue());

            if (userOptional.isEmpty() || !userOptional.get().isEnabled() || !userOptional.get().isAccountNonExpired() || !userOptional.get().isAccountNonLocked() || !userOptional.get().isCredentialsNonExpired()) {
                log.warn("User ID {} from token not found in DB or is not active/enabled/non-locked/non-expired.", userId);
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // Si el usuario existe y es válido, cargar los UserDetails completos usando el email
            // Esto es crucial para que Spring Security tenga el objeto UserDetails completo
            // y pueda realizar las comprobaciones de roles correctamente.
            UserDetails userDetails = userDetailsService.loadUserByUsername(userOptional.get().getEmail());

            // Crear el objeto Authentication
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, // Usar el objeto UserDetails completo como principal
                    null,        // Credenciales no necesarias para JWT
                    userDetails.getAuthorities() // Obtener las autoridades directamente de UserDetails
            );

            // Establecer la autenticación en el contexto de seguridad
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);

            log.info("User {} successfully authenticated with role {}.", userDetails.getUsername(), roleName);

            // (Opcional) Agregar ID de usuario a los atributos de la solicitud
            request.setAttribute("X-User-Id", userId);

        } else {
            // Log si ya hay autenticación o si faltan datos del token
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                log.trace("Security context already contains authentication. Skipping token authentication.");
            } else {
                log.warn("Could not authenticate user. UserID or Role missing from token.");
            }
        }

        // 5. Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
