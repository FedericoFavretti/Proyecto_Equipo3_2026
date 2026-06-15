package com.example.demo.Logica.DataTypes.response;

import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.DataTypes.shared.DtPromocion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtBusquedaPlatosPromocionesResponse {
    private List<DtPlato> platos;
    private List<DtPromocion> promociones;
}

