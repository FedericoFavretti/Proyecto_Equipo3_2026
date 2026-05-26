package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.RolUsuario;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UsuarioUserDetails implements UserDetails {

    private final Usuario usuario;

    public UsuarioUserDetails(Usuario usuario) {
        this.usuario = Objects.requireNonNull(usuario, "usuario no puede ser null");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        RolUsuario tipo = usuario.getTipo();
        String authority = tipo != null ? tipo.getAuthority() : "ROLE_USER";
        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    public String getPassword() {
        return usuario.getPasswd();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        EstadoCuenta estado = usuario.getEstado();
        return estado == null || !estado.estaBloqueada();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        EstadoCuenta estado = usuario.getEstado();
        return estado != null && estado.habilitaAutenticacion();
    }
}
