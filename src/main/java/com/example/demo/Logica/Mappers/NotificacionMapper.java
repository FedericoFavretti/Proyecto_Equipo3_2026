package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.Notificacion;
import com.example.demo.Logica.DataTypes.shared.DtNotificacion;
import org.springframework.stereotype.Component;

@Component
public class NotificacionMapper {

   private final ReclamoMapper reclamoMapper;
   private final PedidoMapper pedidoMapper;

   public  NotificacionMapper(ReclamoMapper reclamoMapper, PedidoMapper pedidoMapper) {
       this.reclamoMapper = reclamoMapper;
       this.pedidoMapper = pedidoMapper;
   }

    public Notificacion mapearNotificacionDeDt(DtNotificacion dtNotificacion){
        return Notificacion.builder()
                .id(dtNotificacion.getId())
                .tipo(dtNotificacion.getTipo())
                .mensaje(dtNotificacion.getMensaje())
                .canal(dtNotificacion.getCanal())
                .leida(dtNotificacion.getLeida())
                .fecha(dtNotificacion.getFecha())
                .reclamo(dtNotificacion.getDtReclamo() != null ? reclamoMapper.mapearReclamoDeDt(dtNotificacion.getDtReclamo()) : null)
                .pedido(dtNotificacion.getDtPedido() != null ? pedidoMapper.mapearPedidoDeDt(dtNotificacion.getDtPedido()) : null)
                .destinatarioTipo(dtNotificacion.getDestinatarioTipo())
                .destinatarioId(dtNotificacion.getDestinatarioId())
                .build();
    }

    public DtNotificacion mapearNotificacionDeClase(Notificacion notificacion){
        return DtNotificacion.builder()
                .id(notificacion.getId())
                .tipo(notificacion.getTipo())
                .mensaje(notificacion.getMensaje())
                .canal(notificacion.getCanal())
                .leida(notificacion.getLeida())
                .fecha(notificacion.getFecha())
                .dtReclamo(notificacion.getReclamo() != null ? reclamoMapper.mapearDtReclamoDeClase(notificacion.getReclamo()) : null)
                .dtPedido(notificacion.getPedido() != null ? pedidoMapper.mapearDtPedidoDeClase(notificacion.getPedido()) : null)
                .destinatarioTipo(notificacion.getDestinatarioTipo())
                .destinatarioId(notificacion.getDestinatarioId())
                .build();
    }
}

