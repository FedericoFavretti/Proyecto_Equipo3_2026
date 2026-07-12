package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.response.DtLocalBusquedaResponse;
import com.example.demo.Logica.DataTypes.response.DtLocalPerfilResponse;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.EstadoLocal;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocalMapperTest {

    private final LocalMapper localMapper = new LocalMapper();

    @Test
    void mapearDtLocalBusquedaDeClaseIncluyeFotoPublica() {
        Local local = crearLocal();

        DtLocalBusquedaResponse response = localMapper.mapearDtLocalBusquedaDeClase(local);

        assertThat(response.getFoto()).isEqualTo("https://cdn.foodly.com/local-logo.png");
        assertThat(response.getImagenes()).containsExactly("https://cdn.foodly.com/fachada.png");
    }

    @Test
    void mapearDtLocalDeClaseIncluyeFotoDelLocal() {
        Local local = crearLocal();

        DtLocal dtLocal = localMapper.mapearDtLocalDeClase(local);

        assertThat(dtLocal.getFoto()).isEqualTo("https://cdn.foodly.com/local-logo.png");
    }

    @Test
    void mapearDtLocalPerfilDeClaseMantieneLaMismaFotoPublica() {
        Local local = crearLocal();

        DtLocalPerfilResponse response = localMapper.mapearDtLocalPerfilDeClase(local);

        assertThat(response.getFoto()).isEqualTo("https://cdn.foodly.com/local-logo.png");
        assertThat(response.getCelular()).isEqualTo("+59899123456");
    }

    private Local crearLocal() {
        return Local.builder()
                .id(7L)
                .email("mc@foodly.com")
                .foto("https://cdn.foodly.com/local-logo.png")
                .estado(EstadoCuenta.Activo)
                .tipo("local")
                .nombre("McDonald's")
                .celular("+59899123456")
                .direccion(DtDireccion.builder()
                        .calle("Av. Italia")
                        .numero("1234")
                        .ciudad("Montevideo")
                        .codigoPostal("11500")
                        .build())
                .descripcion("Hamburguesas")
                .estadoLocal(EstadoLocal.Habilitado)
                .calificacionGlobal(4.5)
                .estaAbierto(true)
                .imagenes(List.of("https://cdn.foodly.com/fachada.png"))
                .build();
    }
}
