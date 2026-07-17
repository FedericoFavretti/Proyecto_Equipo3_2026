package com.example.demo.Logica.DataTypes.request;

import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DtGoogleRegistroCompletarRequest {
    private String tokenRegistro;
    private String documento;
    private DtDireccion direccion;
    private Boolean aceptaTerminos;
    private String foto;
}
