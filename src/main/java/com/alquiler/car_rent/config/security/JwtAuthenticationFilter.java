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

    // Ajustar constructor para inyectar UserEntityRepository
    public JwtAuthenticationFilter(JwtService jwtService, UserEntityRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository; // Asignar el repositorio inyectado
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

            // ---- Validación OBLIGATORIA en BD (descomentada y activada) ----
            Optional<UserEntity> userOptional = userRepository.findById(userId.longValue());

            // Verificar si el usuario existe Y si está habilitado (u otras condiciones como no bloqueado)
            if (userOptional.isEmpty() || !userOptional.get().isEnabled()) { // Ajusta las condiciones según tu UserEntity (isEnabled, isAccountNonLocked, etc.)
                log.warn("User ID {} from token not found in DB or is not active/enabled.", userId);
                SecurityContextHolder.clearContext(); // Limpiar contexto si el usuario no es válido
                filterChain.doFilter(request, response); // Continuar, pero sin autenticar
                return; // Salir del filtro para esta solicitud
            }
            // Si el usuario existe y es válido, obtenemos sus detalles
            UserEntity user = userOptional.get();
            // Usar un identificador único y estable del usuario como principal (email o username)
            String principal = user.getEmail(); // O user.getUsername(), dependiendo de lo que uses consistentemente
            // ---- Fin Validación en BD ----


            // *** LA CORRECCIÓN CLAVE (se mantiene igual) ***
            // Crear la autoridad directamente con el nombre del rol extraído del token.
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName));
            log.debug("Creating Authentication object with principal: {} and authorities: {}", principal, authorities);

            // Crear el objeto Authentication
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    principal,   // Principal obtenido de la BD (email/username)
                    null,        // Credenciales no necesarias
                    authorities  // Autoridades (SIN prefijo "ROLE_")
            );

            // Establecer la autenticación en el contexto de seguridad
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);

            log.info("User {} successfully authenticated with role {}.", principal, roleName);

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
