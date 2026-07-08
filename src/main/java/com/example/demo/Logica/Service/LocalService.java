package com.example.demo.Logica.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.stream.Collectors;

import com.example.demo.Logica.Clases.*;
import com.example.demo.Logica.DataTypes.request.DtEstadisticasLocalFiltro;
import com.example.demo.Logica.DataTypes.response.DtPlatoEstadistica;
import com.example.demo.Logica.DataTypes.response.DtEstadisticasLocal;
import com.example.demo.Logica.DataTypes.response.DtVentaMensualEstadistica;
import com.example.demo.Logica.DataTypes.shared.DtCategoria;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.DataTypes.shared.DtPromocion;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Logica.Enums.PeriodoEstadisticasPreset;
import com.example.demo.Logica.Exceptions.AccessDeniedException;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.ResourceConflictException;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Logica.Interfaces.RegistroLocalNotificador;
import com.example.demo.Logica.Mappers.CategoriaMapper;
import com.example.demo.Logica.Mappers.LocalMapper;
import com.example.demo.Logica.Mappers.PlatoMapper;
import com.example.demo.Logica.Record.PlatoVendidoEstadisticaProjection;
import com.example.demo.Logica.Record.VentaMensualEstadisticaProjection;
import com.example.demo.Persistencia.Repositorios.*;
import com.example.demo.Logica.DataTypes.request.DtPromocionRequest;
import com.example.demo.Logica.Mappers.PromocionMapper;
import com.example.demo.Logica.DataTypes.request.DtFiltroClienteLocal;
import com.example.demo.Logica.DataTypes.response.DtClienteLocalResponse;
import com.example.demo.Logica.DataTypes.response.DtLocalPerfilResponse;
import com.example.demo.Logica.DataTypes.response.DtPromocionesLocalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalService {
    private static final Pattern FORMATO_EMAIL =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final String MENSAJE_CAMPOS_REQUERIDOS =
            "Los siguientes campos son requeridos. Por favor, completelos antes de enviar.";
    private static final String MENSAJE_EMAIL_INVALIDO =
            "El correo electronico ingresado no tiene un formato valido.";
    private static final String MENSAJE_IMAGEN_INVALIDA =
            "Solo se aceptan imagenes en formato JPG o PNG de hasta 10 MB cada una.";
    private static final String TIPO_USUARIO_LOCAL = "local";
    private static final String MENSAJE_LOCAL_YA_ABIERTO =
            "El local ya se encuentra registrado como abierto para el dia de hoy.";
    private static final String MENSAJE_LOCAL_NO_ENCONTRADO =
            "El local no fue encontrado.";
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
    private static final String MENSAJE_PLATO_NO_ENCONTRADO =
            "El plato no existe.";
    private static final String MENSAJE_LOCAL_NO_HABILITADO =
            "El local debe estar habilitado para realizar esta operacion.";
    private static final String MENSAJE_NOMBRE_LOCAL_DUPLICADO =
            "El nombre del local ya se encuentra registrado.";
    private static final String MENSAJE_PERIODO_SIN_DATOS =
            "No hay informacion disponible para el periodo seleccionado. Intente con un rango de fechas diferente.";
    private static final String MENSAJE_PERIODO_AMBIGUO =
            "Debe enviar un preset o un rango libre, pero no ambos.";
    private static final String MENSAJE_PERIODO_INCOMPLETO =
            "Para usar rango libre debe indicar fechaDesde y fechaHasta.";
    private static final String MENSAJE_PERIODO_INVALIDO =
            "La fechaDesde no puede ser posterior a fechaHasta.";
    private static final int LIMITE_PLATOS_MAS_PEDIDOS = 5;
    private static final String MENSAJE_CATEGORIA_DE_OTRO_LOCAL = "La catagoria no pertenece a este local";

    private final LocalRepositorio localRepositorio;
    private final PlatoRepositorio platoRepositorio;
    private final RegistroLocalNotificador registroLocalNotificador;
    private final UsuarioRepositorio usuarioRepositorio;
    private final PedidoRepositorio pedidoRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final LocalMapper localMapper;
    private final PlatoMapper platoMapper;
    private final PromocionRepositorio promocionRepositorio;
    private final PromocionMapper promocionMapper;
    private final ClienteRepositorio clienteRepositorio;
    private final CalificacionRepositorio calificacionRepositorio;
    private final CategoriaRepositorio categoriaRepositorio;
    private final CategoriaMapper categoriaMapper;

    public LocalService(LocalRepositorio localRepositorio, PlatoRepositorio platoRepositorio, RegistroLocalNotificador registroLocalNotificador, UsuarioRepositorio usuarioRepositorio, PedidoRepositorio pedidoRepositorio, PasswordEncoder passwordEncoder, LocalMapper localMapper, PlatoMapper platoMapper, PromocionRepositorio promocionRepositorio, PromocionMapper promocionMapper, ClienteRepositorio clienteRepositorio, CalificacionRepositorio calificacionRepositorio, CategoriaRepositorio categoriaRepositorio, CategoriaMapper categoriaMapper) {
        this.localRepositorio = localRepositorio;
        this.platoRepositorio = platoRepositorio;
        this.registroLocalNotificador = registroLocalNotificador;
        this.usuarioRepositorio = usuarioRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.localMapper = localMapper;
        this.platoMapper = platoMapper;
        this.promocionRepositorio = promocionRepositorio;
        this.promocionMapper = promocionMapper;
        this.clienteRepositorio = clienteRepositorio;
        this.calificacionRepositorio = calificacionRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;
        this.categoriaMapper = categoriaMapper;
    }

    @Transactional
    public Plato gestionarPlatoAlta(DtPlato dtPlato) {
        validarDatosPlatoModificacion(dtPlato);
        validarImagenesPlato(dtPlato.getImagen());

        if(platoRepositorio.buscarPorNombre(dtPlato.getNombre()).isPresent()) {
            throw new ResourceConflictException(MENSAJE_PLATO_YA_EXISTE);
        }

        Local local = localRepositorio.buscarPorId(dtPlato.getDtLocal().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Local", dtPlato.getDtLocal().getId()));
        validarLocalHabilitado(local);
        validarCategoriaPerteneceAlLocal(dtPlato, local.getId());
        Plato plato = platoMapper.mapearPlatoDeDt(dtPlato);
        plato.setLocal(local);
        return platoRepositorio.guardar(plato);
    }

    @Transactional
    public Plato gestionarPlatoModificacion(long idPlato, DtPlato dtPlato) {
        validarDatosPlatoModificacion(dtPlato);

        Plato platoExistente = platoRepositorio.buscarPorId(idPlato)
                .orElseThrow(() -> new ResourceNotFoundException("Plato", idPlato));

        if (listaVacia(dtPlato.getImagen())) {
            dtPlato.setImagen(platoExistente.getImagen());
        }
        validarImagenesPlato(dtPlato.getImagen());

        Local local = localRepositorio.buscarPorId(dtPlato.getDtLocal().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Local", dtPlato.getDtLocal().getId()));
        validarLocalHabilitado(local);
        validarPlatoPerteneceAlLocal(platoExistente, local.getId());
        validarCategoriaPerteneceAlLocal(dtPlato, local.getId());

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
    public Promocion altaPromocion(DtPromocionRequest request) {
        validarDatosPromocion(request);

        Plato plato = platoRepositorio.buscarPorId(request.getIdPlato())
                .orElseThrow(() -> new ResourceNotFoundException(MENSAJE_PLATO_NO_ENCONTRADO, request.getIdPlato()));

        Promocion promocion = Promocion.builder()
                .descuento(request.getDescuento())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .descripcion(request.getDescripcion())
                .plato(plato)
                .build();

        promocionRepositorio.guardar(promocion);
        return promocion;
    }

    @Transactional
    public Promocion gestionarPromocionModificacion(long idPromocion, DtPromocionRequest request) {
        validarDatosPromocion(request);

        promocionRepositorio.buscarPorId(idPromocion)
                .orElseThrow(() -> new ResourceNotFoundException("Promocion", idPromocion));

        Plato plato = platoRepositorio.buscarPorId(request.getIdPlato())
                .orElseThrow(() ->new ResourceNotFoundException("Plato", request.getIdPlato()));

        Promocion promocion = Promocion.builder()
                .id(idPromocion)
                .descuento(request.getDescuento())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .descripcion(request.getDescripcion())
                .plato(plato)
                .build();

        promocionRepositorio.actualizar(promocion);
        return promocion;
    }

    @Transactional
    public List<DtCategoria> listarCategoriasDeLocal(Long idLocal) {
        return categoriaMapper.mapearDtCategorias(categoriaRepositorio.listarPorLocal(idLocal));
    }

    @Transactional
    public Categoria altaCategoria(DtCategoria dto) {
        if (textoVacio(dto.getNombre()) || dto.getIdLocal() == null) {
            throw new IllegalArgumentException("Debe indicar nombre de categoría y local.");
        }
        categoriaRepositorio.buscarPorNombreYLocal(dto.getNombre().trim(), dto.getIdLocal())
                .ifPresent(c -> { throw new ResourceConflictException("La categoría ya existe para este local."); });

        return categoriaRepositorio.guardar(
                Categoria.builder().nombre(dto.getNombre().trim()).idLocal(dto.getIdLocal()).build()
        );
    }

    @Transactional
    public void eliminarCategoria(Long idCategoria, Long idLocal) {
        Categoria categoria = categoriaRepositorio.buscarPorId(idCategoria)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", idCategoria));
        if (!categoria.getIdLocal().equals(idLocal)) {
            throw new AccessDeniedException("La categoría no pertenece al local indicado.");
        }
        categoriaRepositorio.eliminar(idCategoria);
    }

    @Transactional
    public void gestionarPromocionBaja(Long idPromocion){
        promocionRepositorio.buscarPorId(idPromocion).orElseThrow(() -> new ResourceNotFoundException("Promocion", idPromocion));
        promocionRepositorio.eliminar(idPromocion);
    }

    @Transactional
    public void solicitarHabilitacion(DtLocal dtLocal) {
        dtLocal.setEstadoCuenta(EstadoCuenta.Pendiente);
        dtLocal.setEstadoLocal(EstadoLocal.Pendiente);
        dtLocal.setTipo(TIPO_USUARIO_LOCAL);
        dtLocal.setEstaAbierto(false);
        dtLocal.setCalificacionGlobal(0.0);
        validarSolicitudRegistroLocal(dtLocal);

        if (localRepositorio.buscarPorNombre(dtLocal.getNombre()).isPresent()) {
            throw new ResourceConflictException(MENSAJE_NOMBRE_LOCAL_DUPLICADO);
        }

        dtLocal.setPasswd(passwordEncoder.encode(dtLocal.getPasswd()));
        Local local = localMapper.mapearLocalDeDt(dtLocal);
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

    @Transactional(readOnly = true)
    public DtEstadisticasLocal obtenerEstadisticasLocal(Long idLocal, DtEstadisticasLocalFiltro filtro) {
        Local local = localRepositorio.buscarPorId(idLocal)
                .orElseThrow(() -> new ResourceNotFoundException("Local", idLocal));
        validarLocalHabilitado(local);

        RangoPeriodo rangoPeriodo = resolverRangoPeriodo(filtro);
        if (!pedidoRepositorio.existePedidoValidoParaEstadisticasEnPeriodo(
                idLocal,
                rangoPeriodo.fechaDesdeInclusive(),
                rangoPeriodo.fechaHastaExclusiva())) {
            throw new BusinessRuleException(MENSAJE_PERIODO_SIN_DATOS);
        }

        List<PlatoVendidoEstadisticaProjection> proyeccionesTop = pedidoRepositorio.obtenerPlatosMasPedidosEnPeriodo(
                idLocal,
                rangoPeriodo.fechaDesdeInclusive(),
                rangoPeriodo.fechaHastaExclusiva(),
                LIMITE_PLATOS_MAS_PEDIDOS);
        List<DtPlatoEstadistica> platosMasPedido = proyeccionesTop.stream()
                .map(this::mapearPlatoEstadistica)
                .toList();
        List<DtPlatoEstadistica> ventasPorPlato = pedidoRepositorio.obtenerVentasPorPlatoEnPeriodo(
                        idLocal,
                        rangoPeriodo.fechaDesdeInclusive(),
                        rangoPeriodo.fechaHastaExclusiva())
                .stream()
                .map(this::mapearPlatoEstadistica)
                .toList();
        Double ventasConfirmadas = pedidoRepositorio.obtenerVentasParaEstadisticasEnPeriodo(
                idLocal,
                rangoPeriodo.fechaDesdeInclusive(),
                rangoPeriodo.fechaHastaExclusiva());
        List<DtVentaMensualEstadistica> ventasMensuales = completarVentasMensuales(
                pedidoRepositorio.obtenerVentasMensualesEnPeriodo(
                        idLocal,
                        rangoPeriodo.fechaDesdeInclusive(),
                        rangoPeriodo.fechaHastaExclusiva()),
                rangoPeriodo.fechaDesde(),
                rangoPeriodo.fechaHasta());
        return DtEstadisticasLocal.builder()
                .fechaDesde(rangoPeriodo.fechaDesde())
                .fechaHasta(rangoPeriodo.fechaHasta())
                .platosMasPedido(platosMasPedido)
                .ventasPorPlato(ventasPorPlato)
                .ventasMensuales(ventasMensuales)
                .ventasConfirmadas(ventasConfirmadas)
                .build();
    }

    @Transactional(readOnly = true)
    public List<DtPlato> buscarPlatosDelocal(Long idLocal) {
        return platoMapper.mapearDtPlatosClase(platoRepositorio.buscarPlatosDelocal(idLocal));
    }

    @Transactional(readOnly = true)
    public DtPromocionesLocalResponse buscaPromocionesDeLocal(Long idLocal) {
        List<Promocion> promociones = promocionRepositorio.buscarPromocionesDelocal(idLocal);
        LocalDate hoy = LocalDate.now();

        List<DtPromocion> vigentes = promociones.stream()
                .filter(promocion -> !promocion.getFechaInicio().toLocalDate().isAfter(hoy)
                        && !promocion.getFechaFin().toLocalDate().isBefore(hoy))
                .map(promocionMapper::mapearDtPromocionDeClase)
                .toList();

        List<DtPromocion> vencidas = promociones.stream()
                .filter(promocion -> promocion.getFechaFin().toLocalDate().isBefore(hoy))
                .map(promocionMapper::mapearDtPromocionDeClase)
                .toList();

        List<DtPromocion> proximas = promociones.stream()
                .filter(promocion -> promocion.getFechaInicio().toLocalDate().isAfter(hoy))
                .map(promocionMapper::mapearDtPromocionDeClase)
                .toList();

        return DtPromocionesLocalResponse.builder()
                .vigentes(vigentes)
                .vencidas(vencidas)
                .proximas(proximas)
                .build();
    }

    @Transactional(readOnly = true)
    public DtLocalPerfilResponse obtenerPerfilPublico(Long idLocal) {
        Local local = localRepositorio.buscarPorId(idLocal)
                .orElseThrow(() -> new ResourceNotFoundException("Local", idLocal));
        validarLocalHabilitado(local);
        return localMapper.mapearDtLocalPerfilDeClase(local);
    }

    @Transactional(readOnly = true)
    public List<DtClienteLocalResponse> buscarYListarClientesDelLocal(Long idLocal, DtFiltroClienteLocal filtro) {
        localRepositorio.buscarPorId(idLocal)
                .orElseThrow(() -> new ResourceNotFoundException(MENSAJE_LOCAL_NO_ENCONTRADO));

        List<Cliente> clientes = clienteRepositorio.buscarClientesDelLocal(idLocal, filtro);

        return clientes.stream()
                .map(cliente -> {
                    var calificacionExistente = calificacionRepositorio
                            .buscarCalificacionLocalACliente(cliente.getId(), idLocal)
                            .orElse(null);

                    return DtClienteLocalResponse.builder()
                            .id(cliente.getId())
                            .nombre(cliente.getNombre())
                            .apellido(cliente.getApellido())
                            .calificacionGlobal(cliente.getCalificacionGlobal())
                            .yaCalificado(calificacionExistente != null)
                            .miPuntaje(calificacionExistente != null ? calificacionExistente.getPuntaje() : null)
                            .miComentario(calificacionExistente != null ? calificacionExistente.getComentario() : null)
                            .build();
                })
                .toList();
    }

    private void validarDatosPromocion(DtPromocionRequest request) {
        if (request == null || request.getIdPlato() == null) {
            throw new IllegalArgumentException("Debe seleccionar al menos un plato para aplicar la promoción.");
        }

        if (request.getDescuento() == null || request.getDescuento() < 1 || request.getDescuento() > 100) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 1% y 100%.");
        }

        if (request.getFechaInicio() == null || request.getFechaFin() == null) {
            throw new IllegalArgumentException("Debe indicar fecha de inicio y fecha de fin de la promoción.");
        }

        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin de la promoción debe ser posterior a la fecha de inicio.");
        }
    }

    private void validarCategoriaPerteneceAlLocal(DtPlato dtPlato, Long idLocal) {
        if (dtPlato.getDtCategoria() == null || dtPlato.getDtCategoria().getId() == null) {
            throw new BusinessRuleException("Debe agregar una categoria.");
        }

        Categoria categoria = categoriaRepositorio.buscarPorId(dtPlato.getDtCategoria().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", dtPlato.getDtCategoria().getId()));

        if (categoria.getIdLocal() == null || !categoria.getIdLocal().equals(idLocal)) {
            throw new BusinessRuleException(MENSAJE_CATEGORIA_DE_OTRO_LOCAL);
        }
    }

    private void validarDatosPlatoModificacion(DtPlato dtPlato) {
        if (dtPlato == null
                || dtPlato.getDtLocal() == null
                || dtPlato.getDtLocal().getId() == null
                || textoVacio(dtPlato.getDescripcion())
                || dtPlato.getDisponible() == null) {
            throw new BusinessRuleException(MENSAJE_DATOS_PLATO_INCOMPLETOS);
        }

        if (textoVacio(dtPlato.getNombre())) {
            throw new BusinessRuleException(MENSAJE_NOMBRE_PLATO_OBLIGATORIO);
        }

        if (precioInvalido(dtPlato.getPrecio())) {
            throw new BusinessRuleException(MENSAJE_PRECIO_PLATO_INVALIDO);
        }
    }

    private void validarImagenesPlato(String imagen) {
        if (listaVacia(imagen)) {
            throw new BusinessRuleException(MENSAJE_DATOS_PLATO_INCOMPLETOS);
        }

        if (imagenPlatoNoPermitida(imagen)) {
            throw new BusinessRuleException(MENSAJE_IMAGEN_PLATO_INVALIDA);
        }
    }

    private boolean textoVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private boolean precioInvalido(Double precio) {
        return precio == null || precio <= 0;
    }

    private boolean listaVacia(String imagen) {
        return imagen == null || imagen.isEmpty();
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
        if (dtLocal == null) {
            throw new BusinessRuleException(String.format(
                    MENSAJE_CAMPOS_REQUERIDOS,
                    "Completar: email, passwd, nombre, calle, numero, ciudad, codigoPostal, descripcion, imagenes"));
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

    private DtPlatoEstadistica mapearPlatoEstadistica(PlatoVendidoEstadisticaProjection projection) {
        Plato plato = platoRepositorio.buscarPorId(projection.idPlato())
                .orElseThrow(() -> new ResourceNotFoundException("Plato", projection.idPlato()));

        return DtPlatoEstadistica.builder()
                .id(plato.getId())
                .nombre(plato.getNombre())
                .imagen(plato.getImagen())
                .cantidadVendida(projection.cantidadTotal())
                .montoVendido(projection.montoTotal())
                .build();
    }

    private List<DtVentaMensualEstadistica> completarVentasMensuales(List<VentaMensualEstadisticaProjection> ventasMensualesAgrupadas,
                                                                     LocalDate fechaDesde,
                                                                     LocalDate fechaHasta) {
        Map<YearMonth, Double> montosPorMes = ventasMensualesAgrupadas.stream()
                .collect(Collectors.toMap(
                        venta -> YearMonth.of(venta.anio(), venta.mes()),
                        VentaMensualEstadisticaProjection::montoVendido
                ));

        List<DtVentaMensualEstadistica> resultado = new ArrayList<>();
        YearMonth mesActual = YearMonth.from(fechaDesde);
        YearMonth ultimoMes = YearMonth.from(fechaHasta);

        while (!mesActual.isAfter(ultimoMes)) {
            resultado.add(DtVentaMensualEstadistica.builder()
                    .anio(mesActual.getYear())
                    .mes(mesActual.getMonthValue())
                    .montoVendido(montosPorMes.getOrDefault(mesActual, 0.0))
                    .build());
            mesActual = mesActual.plusMonths(1);
        }

        return resultado;
    }

    private RangoPeriodo resolverRangoPeriodo(DtEstadisticasLocalFiltro filtro) {
        if (filtro == null || (filtro.getPreset() == null && filtro.getFechaDesde() == null && filtro.getFechaHasta() == null)) {
            return resolverRangoPreset(PeriodoEstadisticasPreset.MES_ACTUAL);
        }

        boolean tienePreset = filtro.getPreset() != null;
        boolean tieneAlgunaFecha = filtro.getFechaDesde() != null || filtro.getFechaHasta() != null;

        if (tienePreset && tieneAlgunaFecha) {
            throw new BusinessRuleException(MENSAJE_PERIODO_AMBIGUO);
        }

        if (tieneAlgunaFecha) {
            if (filtro.getFechaDesde() == null || filtro.getFechaHasta() == null) {
                throw new BusinessRuleException(MENSAJE_PERIODO_INCOMPLETO);
            }
            if (filtro.getFechaDesde().isAfter(filtro.getFechaHasta())) {
                throw new BusinessRuleException(MENSAJE_PERIODO_INVALIDO);
            }
            return construirRango(filtro.getFechaDesde(), filtro.getFechaHasta());
        }

        return resolverRangoPreset(filtro.getPreset());
    }

    private RangoPeriodo resolverRangoPreset(PeriodoEstadisticasPreset preset) {
        LocalDate hoy = LocalDate.now();
        return switch (preset) {
            case HOY -> construirRango(hoy, hoy);
            case ULTIMOS_7_DIAS -> construirRango(hoy.minusDays(6), hoy);
            case ULTIMOS_30_DIAS -> construirRango(hoy.minusDays(29), hoy);
            case MES_ANTERIOR -> {
                LocalDate primerDiaMesAnterior = hoy.minusMonths(1).withDayOfMonth(1);
                yield construirRango(primerDiaMesAnterior, primerDiaMesAnterior.withDayOfMonth(primerDiaMesAnterior.lengthOfMonth()));
            }
            case MES_ACTUAL -> construirRango(hoy.withDayOfMonth(1), hoy);
        };
    }

    private RangoPeriodo construirRango(LocalDate fechaDesde, LocalDate fechaHasta) {
        return new RangoPeriodo(
                fechaDesde,
                fechaHasta,
                fechaDesde.atStartOfDay(),
                fechaHasta.plusDays(1).atStartOfDay()
        );
    }

    private record RangoPeriodo(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            LocalDateTime fechaDesdeInclusive,
            LocalDateTime fechaHastaExclusiva
    ) {
    }

    public void validarPartesMultimediaRegistroLocal(MultipartFile logo, List<MultipartFile> imagenes) {
        List<String> camposFaltantes = new ArrayList<>();
        if (logo == null || logo.isEmpty()) {
            camposFaltantes.add("logo");
        }
        if (imagenes == null || imagenes.isEmpty()) {
            camposFaltantes.add("imagenes");
        }
        if (!camposFaltantes.isEmpty()) {
            throw new BusinessRuleException(
                    String.format(MENSAJE_CAMPOS_REQUERIDOS, String.join(", ", camposFaltantes)));
        }
    }
}

