package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.Clases.Reclamo;
import com.example.demo.Logica.DataTypes.request.DtFiltroReclamo;
import com.example.demo.Logica.DataTypes.shared.DtReclamo;
import com.example.demo.Logica.Mappers.ReclamoMapper;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.ReclamoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

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

    public void reclamar(DtReclamo dtReclamo){
        if(dtReclamo.getMotivo().isEmpty()){
            throw new RuntimeException("Debe ingresar un motivo");
        }
        Pedido pedido = pedidoRepositorio.buscarPorId(dtReclamo.getDtPedido().getId()).orElseThrow(()->new RuntimeException("No existe el pedido con el id"));
        dtReclamo.setMontoReintegro(pedido.getTotal());
        dtReclamo.setFecha(LocalDateTime.now());
        Reclamo reclamo = reclamoMapper.mapearReclamoDeDt(dtReclamo);
        reclamoRepositorio.guardar(reclamo);
    }

    public List<DtReclamo> buscarReclamos(@RequestBody DtFiltroReclamo dtFiltroReclamo){
        if(dtFiltroReclamo.getFechaReclamo() == null && dtFiltroReclamo.getEstadoPedido() == null && dtFiltroReclamo.getIdCliente() == null){
            throw new RuntimeException("Debe ingresar algun filtro para obtener los reclamos");
        }
        return null;
    }
}
