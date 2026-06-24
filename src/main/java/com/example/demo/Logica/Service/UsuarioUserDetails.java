package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Administrador;
import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Logica.Enums.EstadoCuenta;

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
        String rol = "";
        if (usuario instanceof Administrador) {
            rol = "Admin";
        } else if (usuario instanceof Local) {
            rol = "Local";
        } else if (usuario instanceof Cliente) {
            rol = "Cliente";
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
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
        return usuario.getEstado() != EstadoCuenta.Bloqueado;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }


}
