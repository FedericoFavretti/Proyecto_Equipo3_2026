package com.example.demo.Logica.DataTypes.request;

import com.example.demo.Logica.Enums.EstadoLocal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtResolverSolicitudLocalRequest {
    private EstadoLocal estadoObjetivo;
}

