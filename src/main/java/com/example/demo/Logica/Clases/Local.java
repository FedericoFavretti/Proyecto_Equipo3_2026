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
    private EstadoLocal estado;
    private Double calificacionGlobal;
    private Boolean estaAbierto;
    private List<String> imagenes;
    private Instant fechaSolicitudAprobacion;
    private Instant fechaAprobacion;

    public Local(long id, String nombre, DtDireccion direccion, String descripcion, EstadoLocal estado,
                 Double calificacionGlobal, Boolean estaAbierto, List<String> imagenes) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.descripcion = descripcion;
        this.estado = estado;
        this.calificacionGlobal = calificacionGlobal;
        this.estaAbierto = estaAbierto;
        this.imagenes = imagenes;
    }
}
