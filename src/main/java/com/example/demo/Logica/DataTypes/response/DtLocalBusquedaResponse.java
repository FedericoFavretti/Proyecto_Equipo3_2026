package com.example.demo.Logica.DataTypes.response;

import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtLocalBusquedaResponse {
    private Long id;
    private String nombre;
    private DtDireccion direccion;
    private String descripcion;
    private Double calificacionGlobal;
    private Boolean estaAbierto;
    private List<String> imagenes;
}