package com.example.demo.Logica.DataTypes.response;

import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.EstadoLocal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtPerfilUsuarioResponse {
    private Long id;
    private String email;
    private String foto;
    private EstadoCuenta estadoCuenta;
    private String tipo;

    private String nombre;
    private String apellido;
    private String documento;
    private DtDireccion direccion;
    private Double calificacionGlobal;
    private Boolean activo;

    private String descripcion;
    private EstadoLocal estadoLocal;
    private Boolean estaAbierto;
    private List<String> imagenes;

    private String nivelAcceso;
}
