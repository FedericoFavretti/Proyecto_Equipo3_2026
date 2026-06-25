package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.Clases.Reclamo;
import com.example.demo.Logica.DataTypes.request.DtFiltroReclamo;
import com.example.demo.Logica.DataTypes.shared.DtReclamo;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Logica.Mappers.ReclamoMapper;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.ReclamoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReclamoService {
    private static final String MENSAJE_MOTIVO_REQUERIDO = "Debe ingresar un motivo.";
    private static final String MENSAJE_FILTRO_REQUERIDO =
            "Debe ingresar algun filtro para obtener los reclamos.";
    private static final String DATOS_INCOMPLETOS = "Debe completar todos los datos.";

    private final ReclamoRepositorio reclamoRepositorio;
    private final PedidoRepositorio pedidoRepositorio;
    private final ReclamoMapper reclamoMapper;

    public ReclamoService(ReclamoRepositorio reclamoRepositorio, PedidoRepositorio pedidoRepositorio,  ReclamoMapper reclamoMapper) {
        this.reclamoRepositorio = reclamoRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
        this.reclamoMapper = reclamoMapper;
    }

    @Transactional
    public void reclamar(DtReclamo dtReclamo){
        if (dtReclamo == null) {
            throw new BusinessRuleException(DATOS_INCOMPLETOS);
        }
        if (dtReclamo.getMotivo() == null || dtReclamo.getMotivo().isBlank()) {
            throw new BusinessRuleException(MENSAJE_MOTIVO_REQUERIDO);
        }
        Pedido pedido = pedidoRepositorio.buscarPorId(dtReclamo.getDtPedido().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", dtReclamo.getDtPedido().getId()));
        dtReclamo.setMontoReintegro(pedido.getTotal());
        dtReclamo.setFecha(LocalDateTime.now());
        Reclamo reclamo = reclamoMapper.mapearReclamoDeDt(dtReclamo);
        reclamoRepositorio.guardar(reclamo);
    }

    @Transactional
    public List<DtReclamo> buscarReclamos(DtFiltroReclamo dtFiltroReclamo){
        if(dtFiltroReclamo.getFechaReclamo() == null && dtFiltroReclamo.getEstadoPedido() == null && dtFiltroReclamo.getIdCliente() == null){
            throw new BusinessRuleException(MENSAJE_FILTRO_REQUERIDO);
        }
        return reclamoMapper.mapearReclamosDeClase(reclamoRepositorio.buscarReclamosPorFiltro(dtFiltroReclamo));
    }

    @Transactional
    public void resolverReclamo(DtReclamo dtReclamo){
        if(reclamoRepositorio.buscarPorId(dtReclamo.getId()).isEmpty()){
            throw new ResourceNotFoundException("Reclamo", dtReclamo.getId());
        }
        Reclamo reclamo = reclamoMapper.mapearReclamoDeDt(dtReclamo);
        reclamoRepositorio.actualizar(reclamo);
    }
}