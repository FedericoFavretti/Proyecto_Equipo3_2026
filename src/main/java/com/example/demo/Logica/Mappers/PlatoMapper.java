package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlatoMapper {

    private final LocalMapper localMapper;
    private final CategoriaMapper categoriaMapper;

    public PlatoMapper(LocalMapper localMapper, CategoriaMapper categoriaMapper) {
        this.localMapper = localMapper;
        this.categoriaMapper = categoriaMapper;
    }

    public Plato mapearPlatoDeDt(DtPlato dtPlato) {
        return Plato.builder()
                .id(dtPlato.getId())
                .nombre(dtPlato.getNombre())
                .descripcion(dtPlato.getDescripcion())
                .categoria(categoriaMapper.mapearCategoriaDeDt(dtPlato.getDtCategoria()))
                .precio(dtPlato.getPrecio())
                .imagen(dtPlato.getImagen())
                .disponible(dtPlato.getDisponible())
                .local(localMapper.mapearLocalDeDt(dtPlato.getDtLocal()))
                .build();
    }

    public DtPlato mapearDtPlatoDeClase(Plato plato) {
        return DtPlato.builder()
                .id(plato.getId())
                .nombre(plato.getNombre())
                .descripcion(plato.getDescripcion())
                .dtCategoria(categoriaMapper.mapearDtCategoriaDeClase(plato.getCategoria()))
                .precio(plato.getPrecio())
                .precioFinal(plato.getPrecio())
                .tienePromocion(false)
                .imagen(plato.getImagen())
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

