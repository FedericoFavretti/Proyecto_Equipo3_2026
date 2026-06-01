package com.example.demo.Logica.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.DtLocal;
import com.example.demo.Logica.DataTypes.DtPlato;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class LocalService {
    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final String MENSAJE_CAMPOS_REQUERIDOS = "Los siguientes campos son requeridos: %s. Por favor, complételos antes de enviar.";
    private static final String MENSAJE_EMAIL_INVALIDO = "El correo electrónico ingresado no tiene un formato válido.";
    private static final String MENSAJE_IMAGEN_INVALIDA = "Solo se aceptan imágenes en formato JPG o PNG de hasta 10 MB cada una.";

    private final LocalRepositorio localRepositorio;
    private final PlatoRepositorio platoRepositorio;
    private final RegistroLocalNotificador registroLocalNotificador;

    public LocalService(
            LocalRepositorio localRepositorio,
            PlatoRepositorio platoRepositorio,
            RegistroLocalNotificador registroLocalNotificador) {
        this.localRepositorio = localRepositorio;
        this.platoRepositorio = platoRepositorio;
        this.registroLocalNotificador = registroLocalNotificador;
    }

    @Transactional
    public Plato altaPlato(DtPlato dtPlato) {
        if (platoInvalido(dtPlato)) {
            throw new IllegalArgumentException("Debe completar todos los datos del plato.");
        }

        if (platoRepositorio.buscarPorNombre(dtPlato.getNombre()).isPresent()) {
            throw new IllegalArgumentException("El nombre del plato ya existe.");
        }

        Local local = localRepositorio.buscarPorId(dtPlato.getDtLocal().getId())
                .orElseThrow(() -> new RuntimeException("Local no encontrado"));
        validarLocalHabilitado(local);
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
        if (platoInvalido(dtPlato)) {
            throw new IllegalArgumentException("Debe modificar un dato para poder actualizar el plato.");
        }

        if (platoRepositorio.buscarPorNombre(dtPlato.getNombre()).isPresent()) {
            throw new IllegalArgumentException("El nombre del plato ya existe.");
        }

        Local local = localRepositorio.buscarPorId(dtPlato.getDtLocal().getId())
                .orElseThrow(() -> new RuntimeException("Local no encontrado"));
        validarLocalHabilitado(local);
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
        platoRepositorio.eliminar(idPlato);
    }

    @Transactional
    public void solicitarHabilitacion(DtLocal dtLocal){
        solicitarRegistroComoLocalHabilitado(dtLocal);
    }

    @Transactional
    public void solicitarRegistroComoLocalHabilitado(DtLocal dtLocal) {
        validarSolicitudRegistroLocal(dtLocal);

        if (localRepositorio.buscarPorNombre(dtLocal.getNombre()).isPresent()) {
            throw new IllegalArgumentException("El nombre del local ya se encuentra registrado.");
        }


        Local local = new Local(
                
                dtLocal.getId(),
                dtLocal.getNombre(),
                dtLocal.getDireccion(),
                dtLocal.getDescripcion(),
                EstadoLocal.PENDIENTE,
                0.0,
                false,
                new ArrayList<>(dtLocal.getImagenes()),
                dtLocal.getFechaSolicitudAprobacion(),
                dtLocal.getFechaAprobacion()
        );

        localRepositorio.guardar(local);
        registroLocalNotificador.notificarAdministradorSolicitudPendiente(local);
    }

    @Transactional
    public void registrarApertura(long idLocal){
        Local local = localRepositorio.buscarPorId(idLocal).orElseThrow(() -> new RuntimeException("Local no encontrado"));
        validarLocalHabilitado(local);
        local.setEstaAbierto(true);
        localRepositorio.actualizar(local);
    }

    @Transactional
    public void regitrarCierre(long idLocal){
        Local local = localRepositorio.buscarPorId(idLocal).orElseThrow(() -> new RuntimeException("Local no encontrado"));
        validarLocalHabilitado(local);
        local.setEstaAbierto(false);
        localRepositorio.actualizar(local);
    }

    private boolean platoInvalido(DtPlato dtPlato) {
        return dtPlato == null
                || textoVacio(dtPlato.getNombre())
                || textoVacio(dtPlato.getDescripcion())
                || precioInvalido(dtPlato.getPrecio())
                || listaVacia(dtPlato.getImagenes())
                || dtPlato.getDisponible() == null
                || dtPlato.getDtLocal() == null;
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

    private void validarSolicitudRegistroLocal(DtLocal dtLocal) {
        List<String> camposFaltantes = new ArrayList<>();

        if (dtLocal == null) {
            throw new IllegalArgumentException(String.format(MENSAJE_CAMPOS_REQUERIDOS,
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
            throw new IllegalArgumentException(String.format(MENSAJE_CAMPOS_REQUERIDOS, String.join(", ", camposFaltantes)));
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
        return !(nombreNormalizado.endsWith(".jpg") || nombreNormalizado.endsWith(".jpeg") || nombreNormalizado.endsWith(".png"));
    }

    private void validarLocalHabilitado(Local local) {
        if (local.getEstadoLocal() != EstadoLocal.HABILITADO) {
            throw new IllegalStateException("El local debe estar habilitado para realizar esta operación.");
        }
    }
}
