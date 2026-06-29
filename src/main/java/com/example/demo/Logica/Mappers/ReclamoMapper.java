package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.Reclamo;
import com.example.demo.Logica.DataTypes.shared.DtReclamo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReclamoMapper {
    private final PedidoMapper pedidoMapper;

    public ReclamoMapper(PedidoMapper pedidoMapper) {
        this.pedidoMapper = pedidoMapper;
    }

    public Reclamo mapearReclamoDeDt(DtReclamo dtReclamo) {
        return Reclamo.builder()
                .id(dtReclamo.getId())
                .motivo(dtReclamo.getMotivo())
                .tipoCompensacion(dtReclamo.getTipoCompensacion())
                .montoReintegro(dtReclamo.getMontoReintegro())
                .fecha(dtReclamo.getFecha())
                .pedido(pedidoMapper.mapearPedidoDeDt(dtReclamo.getDtPedido()))
                .build();
    }

    public DtReclamo mapearDtReclamoDeClase(Reclamo reclamo) {
        return DtReclamo.builder()
                .id(reclamo.getId())
                .motivo(reclamo.getMotivo())
                .tipoCompensacion(reclamo.getTipoCompensacion())
                .montoReintegro(reclamo.getMontoReintegro())
                .fecha(reclamo.getFecha())
                .dtPedido(pedidoMapper.mapearDtPedidoDeClase(reclamo.getPedido()))
                .build();
    }

    public List<DtReclamo> mapearReclamosDeClase(List<Reclamo> reclamos) {
        return reclamos.stream()
                .map(this::mapearDtReclamoDeClase)
                .collect(Collectors.toList());
    }
}

