package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Logica.DataTypes.request.DtResCuentaUsuario;
import com.example.demo.Logica.DataTypes.request.DtResolverSolicitudLocalRequest;
import com.example.demo.Logica.DataTypes.response.DtSolicitudLocalPendienteResponse;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Logica.Interfaces.RegistroLocalNotificador;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import com.example.demo.Logica.DataTypes.request.DtFiltroUsuario;
import com.example.demo.Logica.DataTypes.response.DtUsuarioListadoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

@Service
public class AdminService {
    private static final String MENSAJE_ESTADO_OBJETIVO_REQUERIDO = "Debe indicar el estado objetivo de la solicitud.";
    private static final String MENSAJE_ID_LOCAL_INVALIDO = "El id del local es obligatorio.";
    private static final String MENSAJE_ESTADO_OBJETIVO_INVALIDO = "El estado objetivo debe ser Habilitado o Rechazado.";
    private static final String MENSAJE_SOLICITUD_NO_PENDIENTE = "Solo se pueden resolver solicitudes en estado Pendiente.";
    private static final String MENSAJE_LOCAL_SIN_CORREO = "El local no tiene correo asociado para notificar la resolucion de la solicitud.";

    private final LocalRepositorio localRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final RegistroLocalNotificador registroLocalNotificador;
    private final ClienteRepositorio clienteRepositorio;
    private final UsuarioService usuarioService;

    public AdminService(
            LocalRepositorio localRepositorio,
            UsuarioRepositorio usuarioRepositorio,
            RegistroLocalNotificador registroLocalNotificador, ClienteRepositorio clienteRepositorio, UsuarioService usuarioService) {
        this.localRepositorio = localRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.registroLocalNotificador = registroLocalNotificador;
        this.clienteRepositorio = clienteRepositorio;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<DtSolicitudLocalPendienteResponse> listarSolicitudesPendientes() {
        return localRepositorio.listarPendientes().stream()
                .map(this::mapearPendiente)
                .toList();
    }

    @Transactional
    public void resolverSolicitud(DtResolverSolicitudLocalRequest request) {
        if (request == null || request.getEstadoObjetivo() == null) {
            throw new BusinessRuleException(MENSAJE_ESTADO_OBJETIVO_REQUERIDO);
        }
        validarIdLocal(request.getIdLocal());

        EstadoLocal estadoObjetivo = request.getEstadoObjetivo();
        validarEstadoObjetivo(estadoObjetivo);

        Local local = localRepositorio.buscarPorId(request.getIdLocal())
                .orElseThrow(() -> new ResourceNotFoundException("Local", request.getIdLocal()));

        if (local.getEstadoLocal() != EstadoLocal.Pendiente) {
            throw new BusinessRuleException(MENSAJE_SOLICITUD_NO_PENDIENTE);
        }
        validarCorreoLocal(local);

        local.setEstadoLocal(estadoObjetivo);
        local.setEstaAbierto(false);
        EstadoCuenta estadoCuenta = estadoCuentaSegunResolucion(estadoObjetivo);
        local.setEstado(estadoCuenta);
        usuarioRepositorio.actualizarEstado(local.getId(), estadoCuenta);
        localRepositorio.actualizar(local);
        registroLocalNotificador.notificarLocalResolucionSolicitud(local);
    }

    @Transactional
    public void resolverCuentaUsuario(DtResCuentaUsuario dtResCuentaUsuario) {
        Usuario usuario = usuarioRepositorio.buscarPorId(dtResCuentaUsuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dtResCuentaUsuario.getId()));

        boolean activar = dtResCuentaUsuario.getActivo();

        if (usuario instanceof Cliente cliente) {
            cliente.setEstado(activar ? EstadoCuenta.Activo : EstadoCuenta.Bloqueado);
            cliente.setActivo(activar);
            clienteRepositorio.actualizar(cliente);
        } else if (usuario instanceof Local local) {
            local.setEstado(activar ? EstadoCuenta.Activo : EstadoCuenta.Bloqueado);
            local.setEstadoLocal(activar ? EstadoLocal.Habilitado : EstadoLocal.Bloqueado);
            localRepositorio.actualizar(local);
        }

        if (!activar) {
            usuario.setSesionesInvalidadasDesde(LocalDateTime.now());
            usuarioRepositorio.actualizar(usuario);
        }
    }

    private void validarIdLocal(Long idLocal) {
        if (idLocal == null || idLocal <= 0) {
            throw new BusinessRuleException(MENSAJE_ID_LOCAL_INVALIDO);
        }
    }

    private void validarEstadoObjetivo(EstadoLocal estadoObjetivo) {
        if (estadoObjetivo != EstadoLocal.Habilitado && estadoObjetivo != EstadoLocal.Rechazado) {
            throw new BusinessRuleException(MENSAJE_ESTADO_OBJETIVO_INVALIDO);
        }
    }

    private void validarCorreoLocal(Local local) {
        if (local.getEmail() == null || local.getEmail().isBlank()) {
            throw new BusinessRuleException(MENSAJE_LOCAL_SIN_CORREO);
        }
    }

    private EstadoCuenta estadoCuentaSegunResolucion(EstadoLocal estadoObjetivo) {
        return estadoObjetivo == EstadoLocal.Habilitado
                ? EstadoCuenta.Activo
                : EstadoCuenta.Bloqueado;
    }

    private DtSolicitudLocalPendienteResponse mapearPendiente(Local local) {
        return new DtSolicitudLocalPendienteResponse(
                local.getId(),
                local.getEmail(),
                local.getNombre(),
                local.getDireccion(),
                local.getDescripcion(),
                local.getImagenes()
        );
    }

    @Transactional(readOnly = true)
    public List<DtUsuarioListadoResponse> buscarYListarUsuarios(DtFiltroUsuario filtro) {
        List<DtUsuarioListadoResponse> usuarios = new ArrayList<>();

        boolean incluirClientes = filtro == null || filtro.getTipoUsuario() == null
                || "cliente".equalsIgnoreCase(filtro.getTipoUsuario());
        boolean incluirLocales = filtro == null || filtro.getTipoUsuario() == null
                || "local".equalsIgnoreCase(filtro.getTipoUsuario());

        if (incluirClientes) {
            clienteRepositorio.buscarConFiltros(filtro).forEach(cliente -> usuarios.add(
                    DtUsuarioListadoResponse.builder()
                            .id(cliente.getId())
                            .email(cliente.getEmail())
                            .tipoUsuario("cliente")
                            .nombreVisible(cliente.getNombre() + " " + cliente.getApellido())
                            .estado(cliente.getEstado())
                            .calificacionGlobal(cliente.getCalificacionGlobal())
                            .build()
            ));
        }

        if (incluirLocales) {
            localRepositorio.buscarUsuariosConFiltros(filtro).forEach(local -> usuarios.add(
                    DtUsuarioListadoResponse.builder()
                            .id(local.getId())
                            .email(local.getEmail())
                            .tipoUsuario("local")
                            .nombreVisible(local.getNombre())
                            .estado(local.getEstado())
                            .calificacionGlobal(local.getCalificacionGlobal())
                            .build()
            ));
        }

        boolean descendente = filtro == null || filtro.getDireccion() == null
                || !"asc".equalsIgnoreCase(filtro.getDireccion());
        Comparator<DtUsuarioListadoResponse> comparador = Comparator.comparing(
                DtUsuarioListadoResponse::getCalificacionGlobal,
                Comparator.nullsLast(Comparator.naturalOrder())
        );
        usuarios.sort(descendente ? comparador.reversed() : comparador);

        if (usuarios.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron usuarios que coincidan con los criterios de búsqueda seleccionados.");
        }

        return usuarios;
    }
}

