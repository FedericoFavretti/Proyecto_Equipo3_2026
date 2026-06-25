package com.example.demo.jwt;

import java.io.IOException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UsuarioRepositorio usuarioRepositorio;
    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsService userDetailsService,
                                   UsuarioRepositorio usuarioRepositorio) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(BEARER_PREFIX.length());
        String username;

        try {
            username = jwtService.extractUsername(jwt);
        } catch (JwtException | IllegalArgumentException exception) {
            LOGGER.warn("No se pudo extraer el username del JWT para {} {}: {}",
                    request.getMethod(), request.getRequestURI(), exception.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                Usuario usuario = usuarioRepositorio.buscarPorEmail(username).orElse(null);
                LocalDateTime sesionesInvalidadasDesde = usuario != null ? usuario.getSesionesInvalidadasDesde() : null;

                if (jwtService.isTokenValid(jwt, userDetails, sesionesInvalidadasDesde)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    LOGGER.debug("JWT autenticado correctamente para {}", username);
                } else {
                    LOGGER.warn("JWT inválido o sesión invalidada para {} en {} {}",
                            username, request.getMethod(), request.getRequestURI());
                }
            } catch (UsernameNotFoundException exception) {
                LOGGER.warn("El usuario {} del JWT no existe en base de datos para {} {}",
                        username, request.getMethod(), request.getRequestURI());
            } catch (JwtException | IllegalArgumentException exception) {
                LOGGER.warn("No se pudo validar el JWT del usuario {} en {} {}: {}",
                        username, request.getMethod(), request.getRequestURI(), exception.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
