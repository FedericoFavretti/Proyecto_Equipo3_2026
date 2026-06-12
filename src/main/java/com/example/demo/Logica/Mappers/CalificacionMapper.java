package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.Calificacion;
import com.example.demo.Logica.DataTypes.DtCalificacion;
import org.springframework.stereotype.Component;

@Component
public class CalificacionMapper {

    private final ClienteMapper clienteMapper;
    private final LocalMapper localMapper;

    public CalificacionMapper(ClienteMapper clienteMapper, LocalMapper localMapper) {
        this.clienteMapper = clienteMapper;
        this.localMapper = localMapper;
    }

    public Calificacion mapearCalificacionDeDt(DtCalificacion dtCalificacion) {
        return Calificacion.builder()
                .id(dtCalificacion.getId())
                .puntaje(dtCalificacion.getPuntaje())
                .comentario(dtCalificacion.getComentario())
                .fecha(dtCalificacion.getFecha())
                .tipo(dtCalificacion.getTipo())
                .cliente(clienteMapper.mapearClienteDeDt(dtCalificacion.getDtCliente()))
                .local(localMapper.mapearLocalDeDt(dtCalificacion.getDtLocal()))
                .build();
    }

    public DtCalificacion mapearDtCalificacionDeClase(Calificacion calificacion) {
        return DtCalificacion.builder()
                .id(calificacion.getId())
                .puntaje(calificacion.getPuntaje())
                .comentario(calificacion.getComentario())
                .fecha(calificacion.getFecha())
                .tipo(calificacion.getTipo())
                .dtCliente(clienteMapper.mapearDtClienteDeClase(calificacion.getCliente()))
                .dtLocal(localMapper.mapearDtLocalDeClase(calificacion.getLocal()))
                .build();
    }
}
