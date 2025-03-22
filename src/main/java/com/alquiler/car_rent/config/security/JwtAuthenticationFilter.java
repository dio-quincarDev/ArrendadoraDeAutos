package com.alquiler.car_rent.config.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.alquiler.car_rent.commons.entities.UserEntity;
import com.alquiler.car_rent.repositories.UserEntityRepository;
import com.alquiler.car_rent.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserEntityRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserEntityRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Extraer token del header
        String token = getTokenFromRequest(request);
        
        // Validar el token
        if (StringUtils.hasText(token) && !jwtService.isExpired(token)) {
            try {
                // Extraer ID de usuario del token
                Integer userEntityId = jwtService.extractUserEntityId(token);
                
                if (userEntityId != null) {
                    // Buscar el usuario en la base de datos
                    Optional<UserEntity> userOptional = userRepository.findById(userEntityId.longValue());
                    
                    if (userOptional.isPresent()) {
                        UserEntity user = userOptional.get();
                        
                        // Crear la autenticación con las autoridades del usuario
                        var authorities = Collections.singletonList(
                            new SimpleGrantedAuthority(user.getRole().name())
                        );
                        
                        var authentication = new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            null,
                            authorities
                        );
                        
                        // Establecer la autenticación en el contexto de seguridad
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        // Agregar ID de usuario a los atributos de la solicitud
                        request.setAttribute("X-User-Id", userEntityId);
                    }
                }
            } catch (Exception e) {
                logger.error("Error al autenticar usuario: ", e);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}