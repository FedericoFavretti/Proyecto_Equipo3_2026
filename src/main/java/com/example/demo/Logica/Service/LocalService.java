package com.example.demo.Logica.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.DtLocal;
import com.example.demo.Logica.DataTypes.DtPlato;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Logica.Interfaces.RegistroLocalNotificador;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final String MENSAJE_PLATO_NO_ENCONTRADO =
            "Plato no encontrado";
    private static final String MENSAJE_PLATO_DE_OTRO_LOCAL =
            "El plato no pertenece al local indicado.";

    @Autowired
    private final LocalRepositorio localRepositorio;
    @Autowired
    private final PlatoRepositorio platoRepositorio;
    @Autowired
    private final RegistroLocalNotificador registroLocalNotificador;
    @Autowired
    private final UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private final PedidoRepositorio pedidoRepositorio;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public LocalService(
            LocalRepositorio localRepositorio,
            PlatoRepositorio platoRepositorio,
            RegistroLocalNotificador registroLocalNotificador,
            UsuarioRepositorio usuarioRepositorio,
            PedidoRepositorio pedidoRepositorio) {
        this.localRepositorio = localRepositorio;
        this.platoRepositorio = platoRepositorio;
        this.registroLocalNotificador = registroLocalNotificador;
        this.usuarioRepositorio = usuarioRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
    }

    @Transactional
    public Plato altaPlato(DtPlato dtPlato) {
        validarDatosPlato(dtPlato);

        if (platoRepositorio.buscarPorNombre(dtPlato.getNombre()).isPresent()) {
            throw new IllegalArgumentException(MENSAJE_PLATO_YA_EXISTE);
        }

        Local local = localRepositorio.buscarPorId(dtPlato.getDtLocal().getId())
                .orElseThrow(() -> new RuntimeException("Local no encontrado"));
        validarLocalHabilitado(local);
        local.setId(dtPlato.getDtLocal().getId());

        Plato plato = Plato.builder()
                .nombre(dtPlato.getNombre())
                .descripcion(dtPlato.getDescripcion())
                .precio(dtPlato.getPrecio())
                .imagenes(dtPlato.getImagenes())
                .disponible(dtPlato.getDisponible())
                .local(local)
                .build();
        return platoRepositorio.guardar(plato);
    }

    @Transactional
    public Plato gestionarPlatoModificacion(long idPlato, DtPlato dtPlato) {
        validarDatosPlato(dtPlato);

        Plato platoExistente = platoRepositorio.buscarPorId(idPlato)
                .orElseThrow(() -> new RuntimeException(MENSAJE_PLATO_NO_ENCONTRADO));

        Local local = localRepositorio.buscarPorId(dtPlato.getDtLocal().getId())
                .orElseThrow(() -> new RuntimeException("Local no encontrado"));
        validarLocalHabilitado(local);
        validarPlatoPerteneceAlLocal(platoExistente, local.getId());

        platoRepositorio.buscarPorNombre(dtPlato.getNombre())
                .filter(plato -> !plato.getId().equals(idPlato))
                .ifPresent(plato -> {
                    throw new IllegalArgumentException(MENSAJE_PLATO_YA_EXISTE);
                });

        Plato plato = Plato.builder()
                .id(idPlato)
                .nombre(dtPlato.getNombre())
                .descripcion(dtPlato.getDescripcion())
                .precio(dtPlato.getPrecio())
                .imagenes(dtPlato.getImagenes())
                .disponible(dtPlato.getDisponible())
                .local(local)
                .build();
        return platoRepositorio.actualizar(plato);
    }

    @Transactional
    public void gestionarPlatoBaja(long idPlato) {
        Plato plato = platoRepositorio.buscarPorId(idPlato)
                .orElseThrow(() -> new RuntimeException(MENSAJE_PLATO_NO_ENCONTRADO));
        plato.setDisponible(false);
        platoRepositorio.actualizar(plato);
    }

    @Transactional
    public void solicitarRegistroComoLocalHabilitado(DtLocal dtLocal) {
        validarSolicitudRegistroLocal(dtLocal);

        if (localRepositorio.buscarPorNombre(dtLocal.getNombre()).isPresent()) {
            throw new IllegalArgumentException("El nombre del local ya se encuentra registrado.");
        }

        Local local = Local.builder()
                .email(dtLocal.getEmail())
                .passwd(passwordEncoder.encode(dtLocal.getPasswd()))
                .foto(dtLocal.getFoto())
                .tipo("Local")
                .nombre(dtLocal.getNombre())
                .direccion(dtLocal.getDireccion())
                .descripcion(dtLocal.getDescripcion())
                .estadoLocal(EstadoLocal.Pendiente)
                .calificacionGlobal(0.0)
                .estaAbierto(false)
                .imagenes(new ArrayList<>(dtLocal.getImagenes()))
                .build();
        local.setEstado(EstadoCuenta.Pendiente);
        usuarioRepositorio.guardar(local);
        localRepositorio.guardar(local);
        registroLocalNotificador.notificarAdministradorSolicitudPendiente(local);
    }

    @Transactional
    public void registrarApertura(long idLocal) {
        Local local = localRepositorio.buscarPorId(idLocal)
                .orElseThrow(() -> new RuntimeException("Local no encontrado"));
        validarLocalHabilitado(local);
        validarLocalCerrado(local);
        local.setEstaAbierto(true);
        localRepositorio.actualizar(local);
    }

    @Transactional
    public void regitrarCierre(long idLocal) {
        Local local = localRepositorio.buscarPorId(idLocal)
                .orElseThrow(() -> new RuntimeException("Local no encontrado"));
        validarLocalHabilitado(local);
        validarLocalAbierto(local);
        validarSinPedidosPendientes(idLocal);
        local.setEstaAbierto(false);
        localRepositorio.actualizar(local);
    }

    private void validarDatosPlato(DtPlato dtPlato) {
        if (dtPlato == null
                || dtPlato.getDtLocal() == null
                || dtPlato.getDtLocal().getId() == null
                || textoVacio(dtPlato.getDescripcion())
                || listaVacia(dtPlato.getImagenes())
                || dtPlato.getDisponible() == null) {
            throw new IllegalArgumentException(MENSAJE_DATOS_PLATO_INCOMPLETOS);
        }

        if (textoVacio(dtPlato.getNombre())) {
            throw new IllegalArgumentException(MENSAJE_NOMBRE_PLATO_OBLIGATORIO);
        }

        if (precioInvalido(dtPlato.getPrecio())) {
            throw new IllegalArgumentException(MENSAJE_PRECIO_PLATO_INVALIDO);
        }

        if (dtPlato.getImagenes().stream().anyMatch(this::imagenPlatoNoPermitida)) {
            throw new IllegalArgumentException(MENSAJE_IMAGEN_PLATO_INVALIDA);
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
            throw new IllegalArgumentException(String.format(
                    MENSAJE_CAMPOS_REQUERIDOS,
                    "email, nombre, calle, numero, ciudad, codigoPostal, descripcion, imagenes"));
        }

        agregarSiVacio(camposFaltantes, "email", dtLocal.getEmail());
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
            throw new IllegalArgumentException(
                    String.format(MENSAJE_CAMPOS_REQUERIDOS, String.join(", ", camposFaltantes)));
        }

        if (!FORMATO_EMAIL.matcher(dtLocal.getEmail()).matches()) {
            throw new IllegalArgumentException(MENSAJE_EMAIL_INVALIDO);
        }

        if (dtLocal.getImagenes().stream().anyMatch(this::imagenNoPermitida)) {
            throw new IllegalArgumentException(MENSAJE_IMAGEN_INVALIDA);
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
            throw new IllegalStateException("El local debe estar habilitado para realizar esta operacion.");
        }
    }

    private void validarLocalCerrado(Local local) {
        if (Boolean.TRUE.equals(local.getEstaAbierto())) {
            throw new IllegalStateException(MENSAJE_LOCAL_YA_ABIERTO);
        }
    }

    private void validarLocalAbierto(Local local) {
        if (!Boolean.TRUE.equals(local.getEstaAbierto())) {
            throw new IllegalStateException(MENSAJE_LOCAL_YA_CERRADO);
        }
    }

    private void validarSinPedidosPendientes(long idLocal) {
        if (pedidoRepositorio.existePedidoPendientePorLocal(idLocal)) {
            throw new IllegalStateException(MENSAJE_LOCAL_CON_PEDIDOS_PENDIENTES);
        }
    }

    private void validarPlatoPerteneceAlLocal(Plato plato, Long idLocal) {
        if (plato.getLocal() == null || plato.getLocal().getId() == null || !plato.getLocal().getId().equals(idLocal)) {
            throw new IllegalStateException(MENSAJE_PLATO_DE_OTRO_LOCAL);
        }
    }
}
