package com.example.demo.Logica.Service;

import com.example.demo.Logica.Mappers.ReclamoMapper;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.ReclamoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReclamoService {
    private final ReclamoRepositorio reclamoRepositorio;
    private final PedidoRepositorio pedidoRepositorio;
    private final ReclamoMapper reclamoMapper;

    public ReclamoService(ReclamoRepositorio reclamoRepositorio, PedidoRepositorio pedidoRepositorio,  ReclamoMapper reclamoMapper) {
        this.reclamoRepositorio = reclamoRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
        this.reclamoMapper = reclamoMapper;
    }
}
