package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlatoMapper {

    private final LocalMapper localMapper;

    public PlatoMapper(LocalMapper localMapper) {
        this.localMapper = localMapper;
    }

    public Plato mapearPlatoDeDt(DtPlato dtPlato) {
        return Plato.builder()
                .id(dtPlato.getId())
                .nombre(dtPlato.getNombre())
                .descripcion(dtPlato.getDescripcion())
                .precio(dtPlato.getPrecio())
                .imagenes(dtPlato.getImagenes())
                .disponible(dtPlato.getDisponible())
                .local(localMapper.mapearLocalDeDt(dtPlato.getDtLocal()))
                .build();
    }

    public DtPlato mapearDtPlatoDeClase(Plato plato) {
        return DtPlato.builder()
                .id(plato.getId())
                .nombre(plato.getNombre())
                .descripcion(plato.getDescripcion())
                .precio(plato.getPrecio())
                .imagenes(plato.getImagenes())
                .disponible(plato.getDisponible())
                .dtLocal(localMapper.mapearDtLocalDeClase(plato.getLocal()))
                .build();
    }

    public List<DtPlato> mapearDtPlatosClase(List<Plato> platos) {
        return platos.stream()
                .map(this::mapearDtPlatoDeClase)
                .collect(Collectors.toList());
    }

}

