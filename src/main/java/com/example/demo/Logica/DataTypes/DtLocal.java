package com.example.demo.Logica.DataTypes;
import com.example.demo.Logica.Enums.EstadoLocal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtLocal extends DtUsuario {
    private long id;
    private String email;
    private String nombre;
    private DtDireccion direccion;
    private String descripcion;
    private EstadoLocal estado;
    private Double calificacionGlobal;
    private boolean estaAbierto;
    private List<String> imagenes;
    private Instant fechaSolicitudAprobacion;
    private Instant fechaAprobacion;
}
