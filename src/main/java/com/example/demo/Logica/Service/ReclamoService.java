package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.Clases.Reclamo;
import com.example.demo.Logica.DataTypes.request.DtFiltroReclamo;
import com.example.demo.Logica.DataTypes.shared.DtPedido;
import com.example.demo.Logica.DataTypes.shared.DtReclamo;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Enums.EstadoReclamo;
import com.example.demo.Logica.Exceptions.AccessDeniedException;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.ResourceConflictException;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Logica.Mappers.PedidoMapper;
import com.example.demo.Logica.Mappers.ReclamoMapper;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.ReclamoRepositorio;
import com.example.demo.Logica.DataTypes.response.DtPagina;
import com.example.demo.Utils.PaginacionUtils;
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
    private static final String MENSAJE_PEDIDO_NO_RECLAMABLE =
            "Solo se pueden realizar reclamos sobre pedidos confirmados o entregados.";
    private static final String MENSAJE_RECLAMO_DUPLICADO =
            "Ya existe un reclamo para este pedido.";
    private static final String MENSAJE_PEDIDO_AJENO =
            "No puede realizar reclamos sobre pedidos que no le pertenecen.";

    private final ReclamoRepositorio reclamoRepositorio;
    private final PedidoRepositorio pedidoRepositorio;
    private final ReclamoMapper reclamoMapper;
    private final PedidoMapper pedidoMapper;
    private final NotificarReclamoService notificarReclamoService;

    public ReclamoService(ReclamoRepositorio reclamoRepositorio, PedidoRepositorio pedidoRepositorio,  ReclamoMapper reclamoMapper, PedidoMapper pedidoMapper, NotificarReclamoService notificarReclamoService) {
        this.reclamoRepositorio = reclamoRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
        this.reclamoMapper = reclamoMapper;
        this.pedidoMapper = pedidoMapper;
        this.notificarReclamoService = notificarReclamoService;
    }

    @Transactional
    public void reclamar(String emailAutenticado, DtReclamo dtReclamo){
        if (dtReclamo == null) {
            throw new BusinessRuleException(DATOS_INCOMPLETOS);
        }
        if (emailAutenticado == null || emailAutenticado.isBlank()) {
            throw new AccessDeniedException(MENSAJE_PEDIDO_AJENO);
        }
        if (dtReclamo.getMotivo() == null || dtReclamo.getMotivo().isBlank()) {
            throw new BusinessRuleException(MENSAJE_MOTIVO_REQUERIDO);
        }
        if (dtReclamo.getDtPedido() == null || dtReclamo.getDtPedido().getId() == null) {
            throw new BusinessRuleException(DATOS_INCOMPLETOS);
        }

        dtReclamo.setEstado(EstadoReclamo.Pendiente);
        Pedido pedido = pedidoRepositorio.buscarPorId(dtReclamo.getDtPedido().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", dtReclamo.getDtPedido().getId()));
        validarPedidoReclamable(emailAutenticado, pedido);
        if (reclamoRepositorio.buscarReclamoPorPedido(pedido.getId()).isPresent()) {
            throw new ResourceConflictException(MENSAJE_RECLAMO_DUPLICADO);
        }
        DtPedido dtPedido = pedidoMapper.mapearDtPedidoDeClase(pedido);
        dtReclamo.setDtPedido(dtPedido);
        dtReclamo.setMontoReintegro(pedido.getTotal());
        dtReclamo.setFecha(LocalDateTime.now());
        Reclamo reclamo = reclamoMapper.mapearReclamoDeDt(dtReclamo);
        reclamoRepositorio.guardar(reclamo);
        notificarReclamoService.notificarReclamo(reclamo);
    }

    private void validarPedidoReclamable(String emailAutenticado, Pedido pedido) {
        if (pedido.getCliente() == null || pedido.getCliente().getEmail() == null
                || !pedido.getCliente().getEmail().equalsIgnoreCase(emailAutenticado)) {
            throw new AccessDeniedException(MENSAJE_PEDIDO_AJENO);
        }

        if (pedido.getEstado() != EstadoPedido.Confirmado && pedido.getEstado() != EstadoPedido.Entregado) {
            throw new BusinessRuleException(MENSAJE_PEDIDO_NO_RECLAMABLE);
        }
    }

    @Transactional
    public DtPagina<DtReclamo> buscarReclamos(DtFiltroReclamo dtFiltroReclamo, Integer pagina, Integer tamanio){
        if (dtFiltroReclamo.getFechaReclamo() == null && dtFiltroReclamo.getEstadoPedido() == null
                && dtFiltroReclamo.getIdCliente() == null && dtFiltroReclamo.getEstadoReclamo() == null
                && dtFiltroReclamo.getIdLocal() == null) {
            throw new BusinessRuleException(MENSAJE_FILTRO_REQUERIDO);
        }
        List<DtReclamo> reclamos = reclamoMapper.mapearReclamosDeClase(reclamoRepositorio.buscarReclamosPorFiltro(dtFiltroReclamo));
        return PaginacionUtils.paginar(reclamos, pagina, tamanio);
    }

    @Transactional
    public void resolverReclamo(DtReclamo dtReclamo) {
        Reclamo reclamoExistente = reclamoRepositorio.buscarPorId(dtReclamo.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Reclamo", dtReclamo.getId()));
        if(reclamoExistente.getEstado() != EstadoReclamo.Pendiente){
            throw new BusinessRuleException("El reclamo debe estar en estado pendiente.");
        }
        if(reclamoExistente.getPedido().getLocal().getEstaAbierto() != true){
            throw new BusinessRuleException("El local debe estar abierto para poder resolver un reclamo");
        }
        reclamoExistente.setEstado(dtReclamo.getEstado());
        reclamoExistente.setTipoCompensacion(dtReclamo.getTipoCompensacion());
        if (dtReclamo.getMotivo() != null && !dtReclamo.getMotivo().isBlank()) {
            reclamoExistente.setMotivo(dtReclamo.getMotivo());
        }
        notificarReclamoService.notificarReslucionReclamo(reclamoExistente);
        reclamoRepositorio.actualizar(reclamoExistente);
    }

    @Transactional(readOnly = true)
    public DtReclamo buscarReclamoPropioDePedido(String emailAutenticado, Long idPedido) {
        Pedido pedido = pedidoRepositorio.buscarPorId(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", idPedido));

        if (pedido.getCliente() == null || pedido.getCliente().getEmail() == null
                || !pedido.getCliente().getEmail().equalsIgnoreCase(emailAutenticado)) {
            throw new AccessDeniedException("El pedido no pertenece al usuario autenticado.");
        }

        return reclamoRepositorio.buscarReclamoPorPedido(idPedido)
                .map(reclamoMapper::mapearDtReclamoDeClase)
                .orElse(null);
    }
}
