package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Calificacion;
import com.example.demo.Logica.DataTypes.shared.DtCalificacion;
import com.example.demo.Logica.Enums.TipoCalificacion;
import com.example.demo.Logica.Mappers.CalificacionMapper;
import com.example.demo.Persistencia.Repositorios.CalificacionRepositorio;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CalificacionService {

    private final CalificacionRepositorio calificacionRepositorio;

    private final ClienteRepositorio clienteRepositorio;

    private final LocalRepositorio  localRepositorio;
    private final CalificacionMapper calificacionMapper;

    public CalificacionService(CalificacionRepositorio calificacionRepositorio, ClienteRepositorio clienteRepositorio, LocalRepositorio  localRepositorio, CalificacionMapper calificacionMapper) {
        this.calificacionRepositorio = calificacionRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.localRepositorio = localRepositorio;
        this.calificacionMapper = calificacionMapper;
    }

    public void calificar(DtCalificacion dtCalificacion) {
        if (dtCalificacion.getPuntaje() < 0) {
            throw new IllegalArgumentException("El puntaje no puede ser negativo.");
        }
        if (dtCalificacion.getComentario() == null || dtCalificacion.getComentario().isBlank()) {
            throw new IllegalArgumentException("El comentario no puede estar vacío.");
        }
        if (dtCalificacion.getDtCliente() != null) {
            dtCalificacion.setTipo(TipoCalificacion.Cliente_a_local);
        } else if (dtCalificacion.getDtLocal() != null) {
            dtCalificacion.setTipo(TipoCalificacion.Local_a_cliente);
        } else {
            throw new IllegalArgumentException("Debe indicarse un cliente o un local como origen.");
        }
        dtCalificacion.setFecha(LocalDateTime.now());
        Calificacion calificacion = calificacionMapper.mapearCalificacionDeDt(dtCalificacion);
        calificacionRepositorio.guardar(calificacion);
    }

}
