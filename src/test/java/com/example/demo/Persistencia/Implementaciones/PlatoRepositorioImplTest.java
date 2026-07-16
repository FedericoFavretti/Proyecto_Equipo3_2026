package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.DataTypes.request.DtFiltro;
import com.example.demo.Persistencia.Repositorios.CategoriaRepositorio;
import com.example.demo.Persistencia.Repositorios.DetallePedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatoRepositorioImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private LocalRepositorio localRepositorio;
    @Mock
    private CategoriaRepositorio categoriaRepositorio;
    @Mock
    @Lazy
    private DetallePedidoRepositorio detallePedidoRepositorio;


    private PlatoRepositorioImpl platoRepositorio;

    @BeforeEach
    void setUp() {
        platoRepositorio = new PlatoRepositorioImpl(jdbcTemplate, localRepositorio, categoriaRepositorio, detallePedidoRepositorio);
    }

    @Test
    void buscarConFiltrosUsaFullTextSearchConNombreYCategoria() {
        DtFiltro filtro = DtFiltro.builder()
                .nombre("Milanesa")
                .build();

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(List.of());
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

        platoRepositorio.buscarConFiltros(filtro);

        verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class), any(Object[].class));

        assertThat(sqlCaptor.getValue()).contains("LEFT JOIN categoria c ON c.id = p.idcategoria");
        assertThat(sqlCaptor.getValue()).contains("p.disponible = true");
        assertThat(sqlCaptor.getValue()).contains("to_tsvector(");
        assertThat(sqlCaptor.getValue()).contains("'spanish_unaccent'");
        assertThat(sqlCaptor.getValue()).contains("coalesce(p.nombre, '')");
        assertThat(sqlCaptor.getValue()).contains("coalesce(c.nombre, '')");
        assertThat(sqlCaptor.getValue()).contains("@@ websearch_to_tsquery('spanish_unaccent', ?)");
        assertThat(sqlCaptor.getValue()).doesNotContain("ILIKE");
    }
}
