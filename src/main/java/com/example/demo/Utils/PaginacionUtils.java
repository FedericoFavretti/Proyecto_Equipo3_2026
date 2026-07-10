package com.example.demo.Utils;

import com.example.demo.Logica.DataTypes.response.DtPagina;

import java.util.Collections;
import java.util.List;

public final class PaginacionUtils {

    public static final int TAMANIO_PAGINA_POR_DEFECTO = 10;
    public static final int TAMANIO_PAGINA_MAXIMO = 100;

    private PaginacionUtils() {
    }

    public static int normalizarPagina(Integer pagina) {
        return (pagina == null || pagina < 0) ? 0 : pagina;
    }

    public static int normalizarTamanio(Integer tamanio) {
        if (tamanio == null || tamanio <= 0) return TAMANIO_PAGINA_POR_DEFECTO;
        return Math.min(tamanio, TAMANIO_PAGINA_MAXIMO);
    }

    public static <T> DtPagina<T> paginar(List<T> listaCompleta, Integer pagina, Integer tamanio) {
        int paginaNormalizada = normalizarPagina(pagina);
        int tamanioNormalizado = normalizarTamanio(tamanio);

        int totalElementos = listaCompleta.size();
        int totalPaginas = (int) Math.ceil(totalElementos / (double) tamanioNormalizado);
        if (totalPaginas == 0) totalPaginas = 1;

        int desde = paginaNormalizada * tamanioNormalizado;
        List<T> contenido;
        if (desde >= totalElementos) {
            contenido = Collections.emptyList();
        } else {
            int hasta = Math.min(desde + tamanioNormalizado, totalElementos);
            contenido = listaCompleta.subList(desde, hasta);
        }

        return DtPagina.<T>builder()
                .contenido(contenido)
                .paginaActual(paginaNormalizada)
                .tamanioPagina(tamanioNormalizado)
                .totalPaginas(totalPaginas)
                .totalElementos(totalElementos)
                .build();
    }
}