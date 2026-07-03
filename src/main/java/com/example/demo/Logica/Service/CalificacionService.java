package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Calificacion;
import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Logica.DataTypes.response.DtCalificacionDetalleClienteResponse;
import com.example.demo.Logica.DataTypes.response.DtCalificacionDetalleResponse;
import com.example.demo.Logica.DataTypes.response.DtCalificacionGlobalResponse;
import com.example.demo.Logica.DataTypes.response.DtMiCalificacionLocalResponse;
import com.example.demo.Logica.DataTypes.shared.DtCalificacion;
import com.example.demo.Logica.Enums.TipoCalificacion;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Logica.Mappers.CalificacionMapper;
import com.example.demo.Logica.Mappers.ClienteMapper;
import com.example.demo.Logica.Mappers.LocalMapper;
import com.example.demo.Persistencia.Repositorios.CalificacionRepositorio;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
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
    private static final String MENSAJE_CLIENTE_REQUERIDO =
            "Debe indicarse tanto el cliente asociados a la calificación.";
    private static final String MENSAJE_USUARIO_NO_ES_LOCAL =
            "El usuario autenticado no corresponde a un local.";
    private static final String MENSAJE_USUARIO_NO_ES_CLIENTE =
            "El usuario autenticado no corresponde a un cliente.";
    private static final String MENSAJE_LOCAL_REQUERIDO =
            "Debe indicar el local a calificar.";
    private static final String MENSAJE_CLIENTE_A_CALIFICAR_REQUERIDO =
            "Debe indicar el cliente a calificar.";
    private static final String MENSAJE_CLIENTE_SIN_PEDIDOS_EN_LOCAL =
            "Solo puede calificar locales en los que haya realizado al menos un pedido.";
    private static final String MENSAJE_LOCAL_SIN_PEDIDOS_DE_CLIENTE =
            "Solo puede calificar a clientes que hayan realizado al menos un pedido en su local.";

    private final CalificacionRepositorio calificacionRepositorio;
    private final LocalRepositorio localRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final CalificacionMapper calificacionMapper;
    private final ClienteRepositorio clienteRepositorio;
    private final ClienteMapper clienteMapper;
    private final LocalMapper localMapper;
    private final PedidoRepositorio pedidoRepositorio;

    public CalificacionService(
            CalificacionRepositorio calificacionRepositorio,
            LocalRepositorio localRepositorio,
            UsuarioRepositorio usuarioRepositorio,
            CalificacionMapper calificacionMapper,
            ClienteRepositorio clienteRepositorio,
            ClienteMapper clienteMapper,
            LocalMapper localMapper,
            PedidoRepositorio pedidoRepositorio) {
        this.calificacionRepositorio = calificacionRepositorio;
        this.localRepositorio = localRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.calificacionMapper = calificacionMapper;
        this.clienteRepositorio = clienteRepositorio;
        this.clienteMapper = clienteMapper;
        this.localMapper = localMapper;
        this.pedidoRepositorio = pedidoRepositorio;
    }

    @Transactional
    public void calificar(DtCalificacion dtCalificacion, String emailAutenticado) {
        Usuario usuarioAutenticado = usuarioRepositorio.buscarPorEmail(emailAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", emailAutenticado));

        validarPuntaje(dtCalificacion);

        if (usuarioAutenticado instanceof Cliente clienteAutenticado) {
            upsertCalificacionDeClienteALocal(dtCalificacion, clienteAutenticado);
            return;
        }

        if (usuarioAutenticado instanceof Local localAutenticado) {
            crearCalificacionDeLocalACliente(dtCalificacion, localAutenticado);
            return;
        }

        throw new BusinessRuleException("Solo clientes y locales pueden calificar.");
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

    @Transactional(readOnly = true)
    public List<DtCalificacionDetalleClienteResponse> consultarCalificacionesDetalladasDelLocal(String emailAutenticado) {
        Usuario usuario = usuarioRepositorio.buscarPorEmail(emailAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", emailAutenticado));
        if (!(usuario instanceof Local local)) {
            throw new BusinessRuleException(MENSAJE_USUARIO_NO_ES_LOCAL);
        }

        List<Calificacion> calificaciones = calificacionRepositorio.listarPorLocal(local.getId()).stream()
                .filter(c -> c.getTipo() == TipoCalificacion.Cliente_a_local)
                .toList();

        return calificaciones.stream()
                .map(c -> DtCalificacionDetalleClienteResponse.builder()
                        .idCliente(c.getCliente() != null ? c.getCliente().getId() : null)
                        .nombreCliente(nombreCompletoCliente(c.getCliente()))
                        .puntaje(c.getPuntaje())
                        .comentario(c.getComentario())
                        .fecha(c.getFecha())
                        .build())
                .toList();
    }

    private String nombreCompletoCliente(Cliente cliente) {
        if (cliente == null) {
            return "Cliente";
        }
        String nombre = cliente.getNombre() != null ? cliente.getNombre() : "";
        String apellido = cliente.getApellido() != null ? cliente.getApellido() : "";
        String nombreCompleto = (nombre + " " + apellido).trim();
        return nombreCompleto.isEmpty() ? "Cliente" : nombreCompleto;
    }

    @Transactional(readOnly = true)
    public List<DtCalificacionDetalleResponse> consultarCalificacionesDetalladasDelCliente(Long idCliente) {
        List<Calificacion> calificaciones = calificacionRepositorio.listarPorCliente(idCliente).stream()
                .filter(c -> c.getTipo() == TipoCalificacion.Local_a_cliente)
                .toList();

        return calificaciones.stream()
                .map(c -> new DtCalificacionDetalleResponse(
                        c.getLocal().getId(),
                        c.getLocal().getNombre(),
                        c.getPuntaje(),
                        c.getComentario(),
                        c.getFecha()
                ))
                .toList();
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

    @Transactional(readOnly = true)
    public DtCalificacionGlobalResponse consultarCalificacionGlobal(Long idCliente) {
        clienteRepositorio.buscarPorId(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException(MENSAJE_CLIENTE_REQUERIDO));

        List<Calificacion> calificaciones = calificacionRepositorio.listarPorCliente(idCliente);

        if (calificaciones.isEmpty()) {
            throw new IllegalArgumentException("Aún no ha recibido calificaciones de ningún local.");
        }

        double promedio = calificaciones.stream()
                .mapToInt(Calificacion::getPuntaje)
                .average()
                .orElse(0.0);

        Map<Integer, Integer> detalle = new HashMap<>();
        for (int puntaje = 1; puntaje <= 5; puntaje++) {
            detalle.put(puntaje, 0);
        }
        for (Calificacion calificacion : calificaciones) {
            detalle.merge(calificacion.getPuntaje(), 1, Integer::sum);
        }

        return DtCalificacionGlobalResponse.builder()
                .promedio(promedio)
                .totalCalificaciones(calificaciones.size())
                .detallePorPuntuacion(detalle)
                .build();
    }

    @Transactional(readOnly = true)
    public DtMiCalificacionLocalResponse consultarMiCalificacionDeLocal(Long idLocal, String emailAutenticado) {
        Usuario usuario = usuarioRepositorio.buscarPorEmail(emailAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", emailAutenticado));
        if (!(usuario instanceof Cliente clienteAutenticado)) {
            throw new BusinessRuleException(MENSAJE_USUARIO_NO_ES_CLIENTE);
        }
        localRepositorio.buscarPorId(idLocal)
                .orElseThrow(() -> new ResourceNotFoundException("Local", idLocal));

        return calificacionRepositorio.buscarCalificacionClienteALocal(clienteAutenticado.getId(), idLocal)
                .map(calificacion -> DtMiCalificacionLocalResponse.builder()
                        .id(calificacion.getId())
                        .puntaje(calificacion.getPuntaje())
                        .comentario(calificacion.getComentario())
                        .fecha(calificacion.getFecha())
                        .build())
                .orElse(null);
    }

    private void upsertCalificacionDeClienteALocal(DtCalificacion dtCalificacion, Cliente clienteAutenticado) {
        if (dtCalificacion.getDtLocal() == null || dtCalificacion.getDtLocal().getId() == null) {
            throw new BusinessRuleException(MENSAJE_LOCAL_REQUERIDO);
        }

        Long idLocal = dtCalificacion.getDtLocal().getId();
        Local local = localRepositorio.buscarPorId(idLocal)
                .orElseThrow(() -> new ResourceNotFoundException("Local", idLocal));

        validarPedidoPrevioClienteConLocal(clienteAutenticado.getId(), idLocal);

        Calificacion calificacionExistente = calificacionRepositorio
                .buscarCalificacionClienteALocal(clienteAutenticado.getId(), idLocal)
                .orElse(null);

        if (calificacionExistente != null) {
            calificacionExistente.setPuntaje(dtCalificacion.getPuntaje());
            calificacionExistente.setComentario(dtCalificacion.getComentario());
            calificacionExistente.setFecha(LocalDateTime.now());
            calificacionExistente.setTipo(TipoCalificacion.Cliente_a_local);
            calificacionExistente.setCliente(clienteAutenticado);
            calificacionExistente.setLocal(local);
            calificacionRepositorio.actualizar(calificacionExistente);
            sincronizarCalificacionGlobal(calificacionExistente);
            return;
        }

        dtCalificacion.setDtCliente(clienteMapper.mapearDtClienteDeClase(clienteAutenticado));
        dtCalificacion.setDtLocal(localMapper.mapearDtLocalDeClase(local));
        dtCalificacion.setTipo(TipoCalificacion.Cliente_a_local);
        dtCalificacion.setFecha(LocalDateTime.now());
        Calificacion nuevaCalificacion = calificacionMapper.mapearCalificacionDeDt(dtCalificacion);
        calificacionRepositorio.guardar(nuevaCalificacion);
        sincronizarCalificacionGlobal(nuevaCalificacion);
    }

    private void crearCalificacionDeLocalACliente(DtCalificacion dtCalificacion, Local localAutenticado) {
        if (dtCalificacion.getDtCliente() == null || dtCalificacion.getDtCliente().getId() == null) {
            throw new BusinessRuleException(MENSAJE_CLIENTE_A_CALIFICAR_REQUERIDO);
        }

        Long idCliente = dtCalificacion.getDtCliente().getId();
        Cliente cliente = clienteRepositorio.buscarPorId(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", idCliente));

        if (!pedidoRepositorio.existePedidoDeClienteEnLocal(idCliente, localAutenticado.getId())) {
            throw new BusinessRuleException(MENSAJE_LOCAL_SIN_PEDIDOS_DE_CLIENTE);
        }

        Calificacion calificacionExistente = calificacionRepositorio
                .buscarCalificacionLocalACliente(idCliente, localAutenticado.getId())
                .orElse(null);

        if (calificacionExistente != null) {
            calificacionExistente.setPuntaje(dtCalificacion.getPuntaje());
            calificacionExistente.setComentario(dtCalificacion.getComentario());
            calificacionExistente.setFecha(LocalDateTime.now());
            calificacionExistente.setTipo(TipoCalificacion.Cliente_a_local);
            calificacionExistente.setCliente(cliente);
            calificacionExistente.setLocal(localAutenticado);
            calificacionRepositorio.actualizar(calificacionExistente);
            sincronizarCalificacionGlobal(calificacionExistente);
            return;
        }

        dtCalificacion.setDtLocal(localMapper.mapearDtLocalDeClase(localAutenticado));
        dtCalificacion.setTipo(TipoCalificacion.Local_a_cliente);
        dtCalificacion.setFecha(LocalDateTime.now());
        Calificacion calificacion = calificacionMapper.mapearCalificacionDeDt(dtCalificacion);
        calificacionRepositorio.guardar(calificacion);
        sincronizarCalificacionGlobal(calificacion);
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

    private void sincronizarCalificacionGlobal(Calificacion calificacion) {
        if (calificacion.getTipo() == null) {
            throw new BusinessRuleException(MENSAJE_CLIENTE_O_LOCAL_REQUERIDO);
        }
        if (calificacion.getTipo() == TipoCalificacion.Cliente_a_local) {
            if (calificacion.getLocal() == null || calificacion.getLocal().getId() == null) {
                throw new BusinessRuleException(MENSAJE_LOCAL_REQUERIDO);
            }
            Local local = localRepositorio.buscarPorId(calificacion.getLocal().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Local", calificacion.getLocal().getId()));
            List<Calificacion> calificaciones = calificacionRepositorio.listarPorLocal(calificacion.getLocal().getId());
            double promedio = calificaciones.stream()
                    .mapToInt(Calificacion::getPuntaje)
                    .average()
                    .orElse(0.0);
            local.setCalificacionGlobal(promedio);
            localRepositorio.actualizar(local);
            return;
        }

        if (calificacion.getCliente() == null || calificacion.getCliente().getId() == null) {
            throw new BusinessRuleException(MENSAJE_CLIENTE_REQUERIDO);
        }
        Cliente cliente = clienteRepositorio.buscarPorId(calificacion.getCliente().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", calificacion.getCliente().getId()));
        List<Calificacion> calificaciones = calificacionRepositorio.listarPorCliente(cliente.getId());
        double promedio = calificaciones.stream()
                .mapToInt(Calificacion::getPuntaje)
                .average()
                .orElse(0.0);
        cliente.setCalificacionGlobal(promedio);
        clienteRepositorio.actualizar(cliente);
    }

    private void validarPuntaje(DtCalificacion dtCalificacion) {
        if (dtCalificacion == null || dtCalificacion.getPuntaje() < 1 || dtCalificacion.getPuntaje() > 5) {
            throw new BusinessRuleException(MENSAJE_PUNTAJE_INVALIDO);
        }
    }

    private void validarPedidoPrevioClienteConLocal(Long idCliente, Long idLocal) {
        if (!pedidoRepositorio.existePedidoDeClienteEnLocal(idCliente, idLocal)) {
            throw new BusinessRuleException(MENSAJE_CLIENTE_SIN_PEDIDOS_EN_LOCAL);
        }
    }
}