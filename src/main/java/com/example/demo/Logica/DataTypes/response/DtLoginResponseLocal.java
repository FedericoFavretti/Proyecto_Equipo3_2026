package com.example.demo.Logica.DataTypes.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.List;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtLoginResponseLocal extends DtLoginResponse {
    private String descripcion;
    private Boolean estaAbierto;
    private List<String> imagenes;
}
