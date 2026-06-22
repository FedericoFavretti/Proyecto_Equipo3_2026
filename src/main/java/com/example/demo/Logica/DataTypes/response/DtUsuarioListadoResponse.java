package com.example.demo.Logica.DataTypes.response;

import com.example.demo.Logica.Enums.EstadoCuenta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtUsuarioListadoResponse {
    private Long id;
    private String email;
    private String tipoUsuario;
    private String nombreVisible;
    private EstadoCuenta estado;
    private Double calificacionGlobal;
}