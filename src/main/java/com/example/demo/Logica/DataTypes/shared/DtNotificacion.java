package com.example.demo.Logica.DataTypes.shared;

import com.example.demo.Logica.Enums.CanalNotificacion;
import com.example.demo.Logica.Enums.TipoDestinatario;
import com.example.demo.Logica.Enums.TipoNotificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtNotificacion {
    private Long id;
    private TipoNotificacion tipo;
    private String mensaje;
    private CanalNotificacion canal;
    private Boolean leida;
    private LocalDateTime fecha;
    private DtPedido dtPedido;
    private DtReclamo dtReclamo;
    private TipoDestinatario destinatarioTipo;
    private Long destinatarioId;
}