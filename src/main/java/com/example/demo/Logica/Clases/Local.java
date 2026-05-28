package com.example.demo.Logica.Clases;
import com.example.demo.Logica.DataTypes.DtDireccion;
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
public class Local extends Usuario {
    private long id;
    private String nombre;
    private DtDireccion direccion;
    private String descripcion;
    private EstadoLocal estadoLocal;
    private Double calificacionGlobal;
    private Boolean estaAbierto;
    private List<String> imagenes;
    private Instant fechaSolicitudAprobacion;
    private Instant fechaAprobacion;
}
