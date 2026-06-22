package com.example.demo.Logica.Clases;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente extends Usuario{
    private String documento;
    private String nombre;
    private String apellido;
    private DtDireccion direccion;
    private Double calificacionGlobal;
    private Boolean activo;
}


