
package com.example.demo.auth.dto;

import com.example.demo.Logica.DataTypes.response.DtUsuarioInfo;

public record AuthResponse(String token, DtUsuarioInfo usuario) {
    public AuthResponse(String token) {
        this(token, null);
    }
}
