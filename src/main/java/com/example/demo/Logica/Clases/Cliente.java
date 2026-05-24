package com.example.demo.Logica.Clases;
import com.example.demo.Logica.DataTypes.DtDireccion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente extends Usuario{
    private long id;
    private String documento;
    private String nombre;
    private String apellido;
    private DtDireccion direccion;
    private Double calificacionGlobal;
    private Boolean activo;
}

