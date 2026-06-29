package com.example.demo.Logica.DataTypes.request;

import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtGoogleAuthRequest {
    private String idToken;
    private DtDireccion direccion;
    private String documento;
}