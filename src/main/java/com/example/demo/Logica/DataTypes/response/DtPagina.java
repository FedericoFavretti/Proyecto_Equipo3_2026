package com.example.demo.Logica.DataTypes.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtPagina<T> {
    private List<T> contenido;
    private int paginaActual;
    private int tamanioPagina;
    private int totalPaginas;
    private long totalElementos;
}