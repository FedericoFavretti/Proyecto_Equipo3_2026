
package com.example.demo.jwt;
import com.example.demo.Logica.DataTypes.shared.DtGoogleUserInfo;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final String secretKey;
    private final Long jwtExpirationMs;

    public JwtService(
            @Value("${security.jwt.secret}") String secretKey,
            @Value("${security.jwt.expiration-ms}") Long jwtExpirationMs) {
        this.secretKey = secretKey;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", userDetails.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("ROLE_USER"));

        return buildToken(extraClaims, userDetails);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = decodeSecret();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] decodeSecret() {
        try {
            return Decoders.BASE64.decode(secretKey);
        } catch (IllegalArgumentException | DecodingException exception) {
            return secretKey.getBytes(StandardCharsets.UTF_8);
        }
    }
    public LocalDateTime getExpiracion(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
    public String generarTokenRecuperacion(String correo) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + 30 * 60 * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("tipo", "RECUPERACION_PASSWORD");

        return Jwts.builder()
                .claims(claims)
                .subject(correo)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String generarTokenRegistroGoogle(DtGoogleUserInfo datosUsuario) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + 15 * 60 * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("tipo", "REGISTRO_GOOGLE_PENDIENTE");
        claims.put("nombre", datosUsuario.getNombre());
        claims.put("apellido", datosUsuario.getApellido());
        claims.put("foto", datosUsuario.getFoto());

        return Jwts.builder()
                .claims(claims)
                .subject(datosUsuario.getEmail())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String validarYObtenerCorreoRecuperacion(String token) {
        Claims claims;
        try {
            claims = extractAllClaims(token);
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("El enlace de recuperación expiró.");
        } catch (JwtException e) {
            throw new IllegalArgumentException("Token inválido.");
        }

        if (!"RECUPERACION_PASSWORD".equals(claims.get("tipo"))) {
            throw new IllegalArgumentException("Token inválido para esta operación.");
        }

        return claims.getSubject();
    }

    public DtGoogleUserInfo validarYObtenerDatosRegistroGoogle(String token) {
        Claims claims;
        try {
            claims = extractAllClaims(token);
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("El registro con Google expiró. Por favor, inicie nuevamente.");
        } catch (JwtException e) {
            throw new IllegalArgumentException("Token de registro Google inválido.");
        }

        if (!"REGISTRO_GOOGLE_PENDIENTE".equals(claims.get("tipo"))) {
            throw new IllegalArgumentException("Token de registro Google inválido.");
        }

        return DtGoogleUserInfo.builder()
                .email(claims.getSubject())
                .nombre((String) claims.get("nombre"))
                .apellido((String) claims.get("apellido"))
                .foto((String) claims.get("foto"))
                .build();
    }

    public boolean isTokenValid(String token, UserDetails userDetails, LocalDateTime sesionesInvalidadasDesde) {
        String username = extractUsername(token);
        Date issuedAt = extractClaim(token, Claims::getIssuedAt);

        boolean noExpirado = !isTokenExpired(token);
        boolean emitidoDespuesDeInvalidacion = sesionesInvalidadasDesde == null
                || issuedAt.toInstant().isAfter(sesionesInvalidadasDesde.atZone(ZoneId.systemDefault()).toInstant());

        return username.equals(userDetails.getUsername()) && noExpirado && emitidoDespuesDeInvalidacion;
    }
}
