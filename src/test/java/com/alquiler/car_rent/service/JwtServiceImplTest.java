
package com.alquiler.car_rent.service;

import com.alquiler.car_rent.commons.dtos.TokenResponse;
import com.alquiler.car_rent.service.impl.JwtServiceImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceImplTest {

    private JwtServiceImpl jwtService;
    private final String secretString = "my-super-secret-key-that-is-long-enough-for-hs256-testing-123456";
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        // Instanciamos el servicio directamente, ya que no tiene dependencias mockeadas.
        jwtService = new JwtServiceImpl(secretString);
        secretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    // Caso de éxito: Generar un token y verificar que sus claims son correctos.
    @Test
    void generateToken_shouldCreateTokenWithCorrectClaims() {
        Long userId = 1L;
        String userRole = "ADMIN";

        TokenResponse tokenResponse = jwtService.generateToken(userId, userRole);
        String token = tokenResponse.getAccesToken();

        assertNotNull(token);

        Integer extractedUserId = jwtService.extractUserEntityId(token);
        String extractedRole = jwtService.extractRole(token);

        assertEquals(userId.intValue(), extractedUserId);
        assertEquals(userRole, extractedRole);
    }

    // Caso de éxito: Un token recién creado no debe estar expirado.
    @Test
    void isExpired_shouldReturnFalse_forActiveToken() {
        TokenResponse tokenResponse = jwtService.generateToken(1L, "USERS");
        assertFalse(jwtService.isExpired(tokenResponse.getAccesToken()));
    }

    // Caso borde: Verificar que un token expirado sea detectado como tal.
    @Test
    void isExpired_shouldReturnTrue_forExpiredToken() {
        // Creamos manualmente un token que expiró hace 1 segundo.
        String expiredToken = Jwts.builder()
                .subject("testUser")
                .issuedAt(new Date(System.currentTimeMillis() - 10000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(secretKey)
                .compact();

        assertTrue(jwtService.isExpired(expiredToken));
    }

    // Caso borde de seguridad: El servicio debe rechazar un token firmado con una clave incorrecta.
    @Test
    void getClaims_shouldThrowException_forTokenWithInvalidSignature() {
        // Creamos una clave secreta diferente a la que usa el servicio.
        SecretKey wrongKey = Keys.hmacShaKeyFor("another-wrong-secret-key-that-is-also-long-enough-for-testing".getBytes(StandardCharsets.UTF_8));

        // Creamos un token con la clave incorrecta.
        String invalidToken = Jwts.builder()
                .subject("testUser")
                .signWith(wrongKey)
                .compact();

        // Verificamos que el servicio lance una excepción al intentar procesar el token.
        assertThrows(IllegalArgumentException.class, () -> {
            jwtService.getClaims(invalidToken);
        });
    }

    // Caso borde: Asegurar que el prefijo "ROLE_" se elimina al guardar el rol en el token.
    @Test
    void generateToken_shouldHandleRolePrefix() {
        TokenResponse tokenResponse = jwtService.generateToken(1L, "ROLE_ADMIN");
        String extractedRole = jwtService.extractRole(tokenResponse.getAccesToken());

        assertEquals("ADMIN", extractedRole); // El rol extraído no debe tener el prefijo.
    }

    // Caso borde de robustez: El constructor debe fallar si la clave secreta es muy corta.
    @Test
    void constructor_shouldThrowException_forWeakSecret() {
        String weakSecret = "short-secret";
        assertThrows(IllegalArgumentException.class, () -> {
            new JwtServiceImpl(weakSecret);
        });
    }

    // Caso borde de robustez: El servicio debe manejar texto que no es un JWT.
    @Test
    void getClaims_shouldThrowException_forMalformedToken() {
        String malformedToken = "this-is-not-a-valid-jwt-token";
        assertThrows(IllegalArgumentException.class, () -> {
            jwtService.getClaims(malformedToken);
        });
    }

    // Caso borde de robustez: Extraer un claim de ID de un token que no lo tiene.
    @Test
    void extractUserEntityId_shouldThrowException_whenClaimIsMissing() {
        // Creamos un token válido pero sin el claim 'userEntityId'.
        String tokenWithoutIdClaim = Jwts.builder()
                .subject("testUser")
                .signWith(secretKey)
                .compact();

        assertThrows(IllegalArgumentException.class, () -> {
            jwtService.extractUserEntityId(tokenWithoutIdClaim);
        });
    }

    // Caso borde de robustez: Extraer un claim de rol de un token que no lo tiene.
    @Test
    void extractRole_shouldThrowException_whenClaimIsMissing() {
        // Creamos un token válido pero sin el claim 'role'.
        String tokenWithoutRoleClaim = Jwts.builder()
                .subject("testUser")
                .claim("userEntityId", 1L) // Incluimos el ID para que no falle por eso
                .signWith(secretKey)
                .compact();

        assertThrows(IllegalArgumentException.class, () -> {
            jwtService.extractRole(tokenWithoutRoleClaim);
        });
    }
}
