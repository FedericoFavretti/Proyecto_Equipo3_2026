package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.DataTypes.request.DtFiltro;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromocionRepositorioImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private PlatoRepositorio platoRepositorio;

    private PromocionRepositorioImpl promocionRepositorio;

    @BeforeEach
    void setUp() {
        promocionRepositorio = new PromocionRepositorioImpl(jdbcTemplate, platoRepositorio);
    }

    @Test
    void buscarActivasConFiltrosUsaBusquedaCaseInsensitiveParaNombreYDescripcion() {
        DtFiltro filtro = DtFiltro.builder()
                .nombre("Cheddar")
                .build();

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(List.of());
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

        promocionRepositorio.buscarActivasConFiltros(filtro);

        verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class), any(Object[].class));

        assertThat(sqlCaptor.getValue()).contains("p.nombre ILIKE ?");
        assertThat(sqlCaptor.getValue()).contains("pr.descripcion ILIKE ?");
        assertThat(sqlCaptor.getValue()).doesNotContain("p.nombre LIKE ?");
        assertThat(sqlCaptor.getValue()).doesNotContain("pr.descripcion LIKE ?");
    }
}
