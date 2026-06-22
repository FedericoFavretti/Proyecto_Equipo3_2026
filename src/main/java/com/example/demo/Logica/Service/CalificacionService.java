package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Calificacion;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Logica.DataTypes.shared.DtCalificacion;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Logica.Enums.TipoCalificacion;
import com.example.demo.Logica.Mappers.CalificacionMapper;
import com.example.demo.Persistencia.Repositorios.CalificacionRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalificacionService {
    private static final String MENSAJE_SIN_CALIFICACIONES =
            "Su local todavía no ha recibido calificaciones de los clientes.";
    private static final String MENSAJE_PUNTAJE_INVALIDO =
            "El puntaje debe estar comprendido entre 1 y 5.";
    private static final String MENSAJE_CLIENTE_O_LOCAL_REQUERIDO =
            "Debe indicarse tanto el cliente como el local asociados a la calificación.";
    private static final String MENSAJE_USUARIO_NO_ES_LOCAL =
            "El usuario autenticado no corresponde a un local.";

    private final CalificacionRepositorio calificacionRepositorio;
    private final LocalRepositorio localRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final CalificacionMapper calificacionMapper;

    public CalificacionService(
            CalificacionRepositorio calificacionRepositorio,
            LocalRepositorio localRepositorio,
            UsuarioRepositorio usuarioRepositorio,
            CalificacionMapper calificacionMapper) {
        this.calificacionRepositorio = calificacionRepositorio;
        this.localRepositorio = localRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.calificacionMapper = calificacionMapper;
    }

    @Transactional
    public void calificar(DtCalificacion dtCalificacion) {
        if (dtCalificacion.getPuntaje() < 1 || dtCalificacion.getPuntaje() > 5) {
            throw new BusinessRuleException(MENSAJE_PUNTAJE_INVALIDO);
        }
        if (dtCalificacion.getDtCliente() == null || dtCalificacion.getDtLocal() == null) {
            throw new BusinessRuleException(MENSAJE_CLIENTE_O_LOCAL_REQUERIDO);
        }
        if (dtCalificacion.getTipo() == null && dtCalificacion.getDtCliente() != null) {
            dtCalificacion.setTipo(TipoCalificacion.Cliente_a_local);
        }
        dtCalificacion.setFecha(LocalDateTime.now());
        Calificacion calificacion = calificacionMapper.mapearCalificacionDeDt(dtCalificacion);
        calificacionRepositorio.guardar(calificacion);
        if (calificacion.getTipo() == TipoCalificacion.Cliente_a_local && calificacion.getLocal() != null) {
            sincronizarCalificacionGlobal(calificacion.getLocal().getId());
        }
    }

    @Transactional
    public Map<String, Object> consultarCalificacionGlobalDelLocal(String emailAutenticado) {
        Usuario usuario = usuarioRepositorio.buscarPorEmail(emailAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", emailAutenticado));
        if (!(usuario instanceof Local local)) {
            throw new BusinessRuleException(MENSAJE_USUARIO_NO_ES_LOCAL);
        }

        return consultarCalificacionGlobalDelLocalPorId(local.getId());
    }

    @Transactional
    public Map<String, Object> consultarCalificacionGlobalDelLocalPorId(Long idLocal) {
        Local local = localRepositorio.buscarPorId(idLocal)
                .orElseThrow(() -> new ResourceNotFoundException("Local", idLocal));
        List<Calificacion> calificaciones = calificacionRepositorio.listarPorLocal(local.getId());
        if (calificaciones.isEmpty()) {
            throw new BusinessRuleException(MENSAJE_SIN_CALIFICACIONES);
        }
        return construirResumenYActualizarCache(local, calificaciones);
    }

    private Map<String, Object> construirResumenYActualizarCache(Local local, List<Calificacion> calificaciones) {
        double promedio = calificaciones.stream()
                .mapToInt(Calificacion::getPuntaje)
                .average()
                .orElse(0.0);

        Map<String, Long> detallePorPuntuacion = new LinkedHashMap<>();
        for (int puntaje = 1; puntaje <= 5; puntaje++) {
            final int puntajeActual = puntaje;
            detallePorPuntuacion.put(
                    String.valueOf(puntaje),
                    calificaciones.stream().filter(calificacion -> calificacion.getPuntaje() == puntajeActual).count()
            );
        }

        local.setCalificacionGlobal(promedio);
        localRepositorio.actualizar(local);

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("calificacionGlobal", promedio);
        resumen.put("totalValoraciones", calificaciones.size());
        resumen.put("detallePorPuntuacion", detallePorPuntuacion);
        return resumen;
    }

    private void sincronizarCalificacionGlobal(Long idLocal) {
        if (idLocal == null) {
            return;
        }
        Local local = localRepositorio.buscarPorId(idLocal)
                .orElseThrow(() -> new ResourceNotFoundException("Local", idLocal));
        List<Calificacion> calificaciones = calificacionRepositorio.listarPorLocal(idLocal);
        double promedio = calificaciones.stream()
                .mapToInt(Calificacion::getPuntaje)
                .average()
                .orElse(0.0);
        local.setCalificacionGlobal(promedio);
        localRepositorio.actualizar(local);
    }
}