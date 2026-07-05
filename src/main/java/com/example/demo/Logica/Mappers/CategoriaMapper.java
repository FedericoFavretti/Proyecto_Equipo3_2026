package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.Categoria;
import com.example.demo.Logica.DataTypes.shared.DtCategoria;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoriaMapper {

    public Categoria mapearCategoriaDeDt(DtCategoria dto) {
        if (dto == null) return null;
        return Categoria.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .idLocal(dto.getIdLocal())
                .build();
    }

    public DtCategoria mapearDtCategoriaDeClase(Categoria categoria) {
        if (categoria == null) return null;
        return DtCategoria.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .idLocal(categoria.getIdLocal())
                .build();
    }

    public List<DtCategoria> mapearDtCategorias(List<Categoria> categorias) {
        return categorias.stream().map(this::mapearDtCategoriaDeClase).collect(Collectors.toList());
    }
}