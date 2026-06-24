package com.example.demo.Logica.DataTypes.response;

import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtPerfilClienteResponse implements DtPerfilDetalleResponse {
    private String nombre;
    private String apellido;
    private String documento;
    private DtDireccion direccion;
    private Double calificacionGlobal;
    private Boolean activo;
}
