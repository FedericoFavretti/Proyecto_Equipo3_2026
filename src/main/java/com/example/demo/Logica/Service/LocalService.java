package com.example.demo.Logica.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.response.DtEstadisticasLocal;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.ResourceConflictException;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Logica.Interfaces.RegistroLocalNotificador;
import com.example.demo.Logica.Mappers.LocalMapper;
import com.example.demo.Logica.Mappers.PlatoMapper;
import com.example.demo.Logica.Record.PlatoMasPedidoProjection;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocalService {
    private static final Pattern FORMATO_EMAIL =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final String MENSAJE_CAMPOS_REQUERIDOS =
            "Los siguientes campos son requeridos: %s. Por favor, completelos antes de enviar.";
    private static final String MENSAJE_EMAIL_INVALIDO =
            "El correo electronico ingresado no tiene un formato valido.";
    private static final String MENSAJE_IMAGEN_INVALIDA =
            "Solo se aceptan imagenes en formato JPG o PNG de hasta 10 MB cada una.";
    private static final String TIPO_USUARIO_LOCAL = "local";
    private static final String MENSAJE_LOCAL_YA_ABIERTO =
            "El local ya se encuentra registrado como abierto para el dia de hoy.";
    private static final String MENSAJE_LOCAL_YA_CERRADO =
            "El local ya se encuentra registrado como cerrado.";
    private static final String MENSAJE_LOCAL_CON_PEDIDOS_PENDIENTES =
            "El local no puede cerrarse porque tiene pedidos pendientes de confirmacion.";
    private static final String MENSAJE_NOMBRE_PLATO_OBLIGATORIO =
            "El nombre del plato es obligatorio.";
    private static final String MENSAJE_PRECIO_PLATO_INVALIDO =
            "El precio debe ser un valor numerico mayor a cero.";
    private static final String MENSAJE_IMAGEN_PLATO_INVALIDA =
            "Solo se aceptan imagenes JPG o PNG.";
    private static final String MENSAJE_DATOS_PLATO_INCOMPLETOS =
            "Debe completar todos los datos del plato.";
    private static final String MENSAJE_PLATO_YA_EXISTE =
            "El nombre del plato ya existe.";
    private static final String MENSAJE_PLATO_DE_OTRO_LOCAL =
            "El plato no pertenece al local indicado.";
    private static final String MENSAJE_LOCAL_NO_HABILITADO =
            "El local debe estar habilitado para realizar esta operacion.";
    private static final String MENSAJE_NOMBRE_LOCAL_DUPLICADO =
            "El nombre del local ya se encuentra registrado.";

    private final LocalRepositorio localRepositorio;
    private final PlatoRepositorio platoRepositorio;
    private final RegistroLocalNotificador registroLocalNotificador;
    private final UsuarioRepositorio usuarioRepositorio;
    private final PedidoRepositorio pedidoRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final LocalMapper localMapper;
    private final PlatoMapper platoMapper;

    public LocalService(
            LocalRepositorio localRepositorio,
            PlatoRepositorio platoRepositorio,
            RegistroLocalNotificador registroLocalNotificador,
            UsuarioRepositorio usuarioRepositorio,
            PedidoRepositorio pedidoRepositorio, PasswordEncoder passwordEncoder, LocalMapper localMapper, PlatoMapper platoMapper) {
        this.localRepositorio = localRepositorio;
        this.platoRepositorio = platoRepositorio;
        this.registroLocalNotificador = registroLocalNotificador;
        this.usuarioRepositorio = usuarioRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.localMapper = localMapper;
        this.platoMapper = platoMapper;
    }

    @Transactional
    public Plato altaPlato(DtPlato dtPlato) {
        validarDatosPlato(dtPlato);

        if (platoRepositorio.buscarPorNombre(dtPlato.getNombre()).isPresent()) {
            throw new ResourceConflictException(MENSAJE_PLATO_YA_EXISTE);
        }

        Local local = localRepositorio.buscarPorId(dtPlato.getDtLocal().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Local", dtPlato.getDtLocal().getId()));
        validarLocalHabilitado(local);
        local.setId(dtPlato.getDtLocal().getId());

        Plato plato = platoMapper.mapearPlatoDeDt(dtPlato);
        plato.setLocal(local);
        return platoRepositorio.guardar(plato);
    }

    @Transactional
    public Plato gestionarPlatoModificacion(long idPlato, DtPlato dtPlato) {
        validarDatosPlato(dtPlato);

        Plato platoExistente = platoRepositorio.buscarPorId(idPlato)
                .orElseThrow(() -> new ResourceNotFoundException("Plato", idPlato));

        Local local = localRepositorio.buscarPorId(dtPlato.getDtLocal().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Local", dtPlato.getDtLocal().getId()));
        validarLocalHabilitado(local);
        validarPlatoPerteneceAlLocal(platoExistente, local.getId());

        platoRepositorio.buscarPorNombre(dtPlato.getNombre())
                .filter(plato -> !plato.getId().equals(idPlato))
                .ifPresent(plato -> {
                    throw new ResourceConflictException(MENSAJE_PLATO_YA_EXISTE);
                });

        Plato plato = platoMapper.mapearPlatoDeDt(dtPlato);
        plato.setId(idPlato);
        plato.setLocal(local);
        return platoRepositorio.actualizar(plato);
    }

    @Transactional
    public void gestionarPlatoBaja(long idPlato) {
        Plato plato = platoRepositorio.buscarPorId(idPlato)
                .orElseThrow(() -> new ResourceNotFoundException("Plato", idPlato));
        plato.setDisponible(false);
        platoRepositorio.actualizar(plato);
    }

    @Transactional
    public void solicitarRegistroComoLocalHabilitado(DtLocal dtLocal) {
        dtLocal.setPasswd(passwordEncoder.encode(dtLocal.getPasswd()));
        dtLocal.setEstadoLocal(EstadoLocal.Pendiente);
        dtLocal.setEstaAbierto(false);
        dtLocal.setCalificacionGlobal(0.0);
        validarSolicitudRegistroLocal(dtLocal);

        if (localRepositorio.buscarPorNombre(dtLocal.getNombre()).isPresent()) {
            throw new ResourceConflictException(MENSAJE_NOMBRE_LOCAL_DUPLICADO);
        }

        Local local = localMapper.mapearLocalDeDt(dtLocal);
        local.setEmail(dtLocal.getEmail().trim());
        local.setPasswd(passwordEncoder.encode(dtLocal.getPasswd().trim()));
        local.setEstado(EstadoCuenta.Pendiente);
        local.setTipo(TIPO_USUARIO_LOCAL);
        local.setNombre(dtLocal.getNombre().trim());
        local.setDescripcion(dtLocal.getDescripcion().trim());
        local.setEstadoLocal(EstadoLocal.Pendiente);
        local.setCalificacionGlobal(0.0);
        local.setEstaAbierto(false);
        usuarioRepositorio.guardar(local);
        localRepositorio.guardar(local);
        registroLocalNotificador.notificarAdministradorSolicitudPendiente(local);
    }

    @Transactional
    public void registrarApertura(long idLocal) {
        Local local = localRepositorio.buscarPorId(idLocal)
                .orElseThrow(() -> new ResourceNotFoundException("Local", idLocal));
        validarLocalHabilitado(local);
        validarLocalCerrado(local);
        local.setEstaAbierto(true);
        localRepositorio.actualizar(local);
    }

    @Transactional
    public void regitrarCierre(long idLocal) {
        Local local = localRepositorio.buscarPorId(idLocal)
                .orElseThrow(() -> new ResourceNotFoundException("Local", idLocal));
        validarLocalHabilitado(local);
        validarLocalAbierto(local);
        validarSinPedidosPendientes(idLocal);
        local.setEstaAbierto(false);
        localRepositorio.actualizar(local);
    }

    @Transactional
    public DtEstadisticasLocal obtenerEstadisticasLocal(Long idLocal) {
        List<PlatoMasPedidoProjection> proyecciones = pedidoRepositorio.obtenerPlatosMasPedidos(idLocal, 5);
        List<DtPlato> platosMasPedido = proyecciones.stream()
                .map(p -> platoRepositorio.buscarPorId(p.idPlato())
                        .orElseThrow(() -> new ResourceNotFoundException("Plato", p.idPlato())))
                .map(platoMapper::mapearDtPlatoDeClase)
                .toList();
        Double gananciasMensuales = pedidoRepositorio.obtenerGananciasMesActual(idLocal);
        return DtEstadisticasLocal.builder()
                .platosMasPedido(platosMasPedido)
                .gananciasMensuales(gananciasMensuales)
                .build();
    }

    private void validarDatosPlato(DtPlato dtPlato) {
        if (dtPlato == null
                || dtPlato.getDtLocal() == null
                || dtPlato.getDtLocal().getId() == null
                || textoVacio(dtPlato.getDescripcion())
                || listaVacia(dtPlato.getImagenes())
                || dtPlato.getDisponible() == null) {
            throw new BusinessRuleException(MENSAJE_DATOS_PLATO_INCOMPLETOS);
        }

        if (textoVacio(dtPlato.getNombre())) {
            throw new BusinessRuleException(MENSAJE_NOMBRE_PLATO_OBLIGATORIO);
        }

        if (precioInvalido(dtPlato.getPrecio())) {
            throw new BusinessRuleException(MENSAJE_PRECIO_PLATO_INVALIDO);
        }

        if (dtPlato.getImagenes().stream().anyMatch(this::imagenPlatoNoPermitida)) {
            throw new BusinessRuleException(MENSAJE_IMAGEN_PLATO_INVALIDA);
        }
    }

    private boolean textoVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private boolean precioInvalido(Double precio) {
        return precio == null || precio <= 0;
    }

    private boolean listaVacia(List<?> lista) {
        return lista == null || lista.isEmpty();
    }

    private boolean imagenPlatoNoPermitida(String imagen) {
        if (imagen == null || imagen.isBlank()) {
            return true;
        }
        String nombreNormalizado = imagen.strip().toLowerCase();
        int queryIndex = nombreNormalizado.indexOf('?');
        if (queryIndex >= 0) {
            nombreNormalizado = nombreNormalizado.substring(0, queryIndex);
        }
        return !(nombreNormalizado.endsWith(".jpg")
                || nombreNormalizado.endsWith(".jpeg")
                || nombreNormalizado.endsWith(".png"));
    }

    private void validarSolicitudRegistroLocal(DtLocal dtLocal) {
        List<String> camposFaltantes = new ArrayList<>();

        if (dtLocal == null) {
            throw new BusinessRuleException(String.format(
                    MENSAJE_CAMPOS_REQUERIDOS,
                    "email, passwd, nombre, calle, numero, ciudad, codigoPostal, descripcion, imagenes"));
        }

        agregarSiVacio(camposFaltantes, "email", dtLocal.getEmail());
        agregarSiVacio(camposFaltantes, "passwd", dtLocal.getPasswd());
        agregarSiVacio(camposFaltantes, "nombre", dtLocal.getNombre());

        if (dtLocal.getDireccion() == null) {
            camposFaltantes.add("calle");
            camposFaltantes.add("numero");
            camposFaltantes.add("ciudad");
            camposFaltantes.add("codigoPostal");
        } else {
            agregarSiVacio(camposFaltantes, "calle", dtLocal.getDireccion().getCalle());
            agregarSiVacio(camposFaltantes, "numero", dtLocal.getDireccion().getNumero());
            agregarSiVacio(camposFaltantes, "ciudad", dtLocal.getDireccion().getCiudad());
            agregarSiVacio(camposFaltantes, "codigoPostal", dtLocal.getDireccion().getCodigoPostal());
        }

        agregarSiVacio(camposFaltantes, "descripcion", dtLocal.getDescripcion());
        if (dtLocal.getImagenes() == null || dtLocal.getImagenes().isEmpty()) {
            camposFaltantes.add("imagenes");
        }

        if (!camposFaltantes.isEmpty()) {
            throw new BusinessRuleException(
                    String.format(MENSAJE_CAMPOS_REQUERIDOS, String.join(", ", camposFaltantes)));
        }

        if (!FORMATO_EMAIL.matcher(dtLocal.getEmail()).matches()) {
            throw new BusinessRuleException(MENSAJE_EMAIL_INVALIDO);
        }

        if (dtLocal.getImagenes().stream().anyMatch(this::imagenNoPermitida)) {
            throw new BusinessRuleException(MENSAJE_IMAGEN_INVALIDA);
        }
    }

    private void agregarSiVacio(List<String> camposFaltantes, String campo, String valor) {
        if (valor == null || valor.isBlank()) {
            camposFaltantes.add(campo);
        }
    }

    private boolean imagenNoPermitida(String imagen) {
        if (imagen == null || imagen.isBlank()) {
            return true;
        }
        String nombreNormalizado = imagen.strip().toLowerCase();
        int queryIndex = nombreNormalizado.indexOf('?');
        if (queryIndex >= 0) {
            nombreNormalizado = nombreNormalizado.substring(0, queryIndex);
        }
        return !(nombreNormalizado.endsWith(".jpg")
                || nombreNormalizado.endsWith(".jpeg")
                || nombreNormalizado.endsWith(".png"));
    }

    private void validarLocalHabilitado(Local local) {
        if (local.getEstadoLocal() != EstadoLocal.Habilitado) {
            throw new BusinessRuleException(MENSAJE_LOCAL_NO_HABILITADO);
        }
    }

    private void validarLocalCerrado(Local local) {
        if (Boolean.TRUE.equals(local.getEstaAbierto())) {
            throw new BusinessRuleException(MENSAJE_LOCAL_YA_ABIERTO);
        }
    }

    private void validarLocalAbierto(Local local) {
        if (!Boolean.TRUE.equals(local.getEstaAbierto())) {
            throw new BusinessRuleException(MENSAJE_LOCAL_YA_CERRADO);
        }
    }

    private void validarSinPedidosPendientes(long idLocal) {
        if (pedidoRepositorio.existePedidoPendientePorLocal(idLocal)) {
            throw new BusinessRuleException(MENSAJE_LOCAL_CON_PEDIDOS_PENDIENTES);
        }
    }

    private void validarPlatoPerteneceAlLocal(Plato plato, Long idLocal) {
        if (plato.getLocal() == null || plato.getLocal().getId() == null || !plato.getLocal().getId().equals(idLocal)) {
            throw new BusinessRuleException(MENSAJE_PLATO_DE_OTRO_LOCAL);
        }
    }
}