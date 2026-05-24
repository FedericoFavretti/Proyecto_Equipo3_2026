package com.example.demo.Logica.DataTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtCliente extends DtUsuario{
    private String documento;
    private String nombre;
    private String apellido;
    private DtDireccion direccion;
    private Double calificacionGlobal;
    private boolean activo;
}
