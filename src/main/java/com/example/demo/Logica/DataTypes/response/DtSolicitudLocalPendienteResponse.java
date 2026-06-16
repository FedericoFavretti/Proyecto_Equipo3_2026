package com.example.demo.Logica.DataTypes.response;

import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtSolicitudLocalPendienteResponse {
    private Long id;
    private String email;
    private String nombre;
    private DtDireccion direccion;
    private String descripcion;
    private List<String> imagenes;
}

