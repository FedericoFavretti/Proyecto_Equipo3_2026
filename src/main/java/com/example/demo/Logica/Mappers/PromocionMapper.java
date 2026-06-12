package com.example.demo.Logica.Mappers;


import com.example.demo.Logica.Clases.Promocion;
import com.example.demo.Logica.DataTypes.DtPromocion;
import org.springframework.stereotype.Component;

@Component
public class PromocionMapper {
    private final PlatoMapper platoMapper;

    public PromocionMapper(PlatoMapper platoMapper) {
        this.platoMapper = platoMapper;
    }

    public Promocion mapearPromocionDeDt(DtPromocion dtPromocion) {
        return Promocion.builder()
                .id(dtPromocion.getId())
                .descuento(dtPromocion.getDescuento())
                .fechaInicio(dtPromocion.getFechaInicio())
                .fechaFin(dtPromocion.getFechaFin())
                .descripcion(dtPromocion.getDescripcion())
                .plato(platoMapper.mapearPlatoDeDt(dtPromocion.getDtPlato()))
                .build();
    }

    public DtPromocion mapearDtPromocionDeClase(Promocion promocion) {
        return DtPromocion.builder()
                .id(promocion.getId())
                .descuento(promocion.getDescuento())
                .fechaInicio(promocion.getFechaInicio())
                .fechaFin(promocion.getFechaFin())
                .descripcion(promocion.getDescripcion())
                .dtPlato(platoMapper.mapearDtPlatoDeClase(promocion.getPlato()))
                .build();
    }
}
