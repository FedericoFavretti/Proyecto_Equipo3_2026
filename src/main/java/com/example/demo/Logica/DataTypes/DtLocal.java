package com.example.demo.Logica.DataTypes;
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
public class DtLocal extends DtUsuario {
    private String nombre;
    private DtDireccion direccion;
    private String descripcion;
    private EstadoLocal estadoLocal;
    private Double calificacionGlobal;
    private Boolean estaAbierto;
    private List<String> imagenes;
}
