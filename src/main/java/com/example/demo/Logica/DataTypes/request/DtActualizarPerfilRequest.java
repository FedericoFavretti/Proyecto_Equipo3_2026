package com.example.demo.Logica.DataTypes.request;

import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtActualizarPerfilRequest {
    private String nombre;
    private String apellido;
    private String descripcion;
    private String email;
    private String password;
    private String celular;
    private String telefonoFijo;
    private DtDireccion direccion;
}
