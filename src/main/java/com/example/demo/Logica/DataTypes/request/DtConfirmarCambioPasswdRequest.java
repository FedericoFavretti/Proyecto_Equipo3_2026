package com.example.demo.Logica.DataTypes.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtConfirmarCambioPasswdRequest {
    private Long idUsuario;
    private String passwdNueva;
    private String passwdConfirmacion;
}