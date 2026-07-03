package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.DetallePedido;
import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.Clases.FacturaDetalle;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.Enums.EstadoFacturaPdf;
import com.example.demo.Persistencia.Repositorios.DetallePedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.FacturaRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @Mock
    private FacturaRepositorio facturaRepositorio;
    @Mock
    private DetallePedidoRepositorio detallePedidoRepositorio;
    @Mock
    private FacturaPdfGeneratorService facturaPdfGeneratorService;
    @Mock
    private FacturaStorageService facturaStorageService;
    @Mock
    private NotificacionPedidoService notificacionPedidoService;

    private FacturaService facturaService;

    @BeforeEach
    void setUp() {
        facturaService = new FacturaService(
                facturaRepositorio,
                detallePedidoRepositorio,
                facturaPdfGeneratorService,
                facturaStorageService,
                notificacionPedidoService,
                3,
                15
        );
    }

    @Test
    void crearFacturaPendienteCongelaSnapshotDelPedido() {
        Pedido pedido = pedidoCompleto();
        when(detallePedidoRepositorio.buscarPorPedido(44L)).thenReturn(List.of(detallePedido(pedido)));

        Factura factura = facturaService.crearFacturaPendiente(pedido);

        ArgumentCaptor<Factura> captor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepositorio).guardar(captor.capture());

        Factura guardada = captor.getValue();
        assertThat(factura.getEstadoPdf()).isEqualTo(EstadoFacturaPdf.PENDIENTE);
        assertThat(guardada.getLocalNombreSnapshot()).isEqualTo("La Cocina");
        assertThat(guardada.getLocalEmailSnapshot()).isEqualTo("local@test.com");
        assertThat(guardada.getClienteNombreSnapshot()).isEqualTo("Ana Perez");
        assertThat(guardada.getClienteEmailSnapshot()).isEqualTo("ana@test.com");
        assertThat(guardada.getDireccionEntregaSnapshot()).contains("Av. Italia").contains("Montevideo");
        assertThat(guardada.getMedioPagoSnapshot()).isEqualTo("Tarjeta");
        assertThat(guardada.getDetalles())
                .singleElement()
                .extracting(FacturaDetalle::getNombreProductoSnapshot, FacturaDetalle::getPrecioUnitario)
                .containsExactly("Milanesa", 15.0);
    }

    @Test
    void procesarFacturaMarcaGeneradaGuardaArchivoYNotifica() throws Exception {
        Factura factura = facturaPendienteConSnapshot();
        byte[] pdf = "pdf".getBytes();

        when(facturaPdfGeneratorService.generarFacturaPdf(any(Factura.class), anyList())).thenReturn(pdf);
        when(facturaStorageService.guardarFacturaPdf(any(Factura.class), any(byte[].class))).thenReturn("facturas/FAC-44.pdf");

        facturaService.procesarFactura(factura);

        ArgumentCaptor<Factura> captor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepositorio, times(2)).actualizarProcesoPdf(captor.capture());

        Factura finalState = captor.getAllValues().getLast();
        assertThat(finalState.getEstadoPdf()).isEqualTo(EstadoFacturaPdf.GENERADA);
        assertThat(finalState.getArchivoPdf()).isEqualTo("facturas/FAC-44.pdf");
        assertThat(finalState.getFechaGeneracionPdf()).isNotNull();
        verify(notificacionPedidoService).notificarFacturaGenerada(finalState, pdf);
    }

    @Test
    void procesarFacturaMantieneEstadoGeneradaSiFallaLaNotificacion() throws Exception {
        Factura factura = facturaPendienteConSnapshot();
        byte[] pdf = "pdf".getBytes();

        when(facturaPdfGeneratorService.generarFacturaPdf(any(Factura.class), anyList())).thenReturn(pdf);
        when(facturaStorageService.guardarFacturaPdf(any(Factura.class), any(byte[].class))).thenReturn("facturas/FAC-44.pdf");
        doThrow(new IllegalStateException("fallo mail"))
                .when(notificacionPedidoService)
                .notificarFacturaGenerada(any(Factura.class), any(byte[].class));

        facturaService.procesarFactura(factura);

        ArgumentCaptor<Factura> captor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepositorio, times(2)).actualizarProcesoPdf(captor.capture());

        Factura finalState = captor.getAllValues().getLast();
        assertThat(finalState.getEstadoPdf()).isEqualTo(EstadoFacturaPdf.GENERADA);
        assertThat(finalState.getArchivoPdf()).isEqualTo("facturas/FAC-44.pdf");
    }

    @Test
    void procesarFacturaMarcaErrorReintentableCuandoAunQuedanIntentos() {
        Factura factura = facturaPendienteConSnapshot();
        when(facturaPdfGeneratorService.generarFacturaPdf(any(Factura.class), anyList()))
                .thenThrow(new IllegalStateException("fallo PDF"));

        facturaService.procesarFactura(factura);

        ArgumentCaptor<Factura> captor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepositorio, times(2)).actualizarProcesoPdf(captor.capture());

        Factura finalState = captor.getAllValues().getLast();
        assertThat(finalState.getEstadoPdf()).isEqualTo(EstadoFacturaPdf.ERROR_REINTENTABLE);
        assertThat(finalState.getIntentosGeneracion()).isEqualTo(1);
        assertThat(finalState.getUltimoErrorPdf()).contains("fallo PDF");
        assertThat(finalState.getProximoReintento()).isNotNull();
    }

    @Test
    void procesarFacturaMarcaErrorFinalCuandoAgotaIntentos() {
        Factura factura = facturaPendienteConSnapshot();
        factura.setIntentosGeneracion(2);
        when(facturaPdfGeneratorService.generarFacturaPdf(any(Factura.class), anyList()))
                .thenThrow(new IllegalStateException("fallo PDF"));

        facturaService.procesarFactura(factura);

        ArgumentCaptor<Factura> captor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepositorio, times(2)).actualizarProcesoPdf(captor.capture());

        Factura finalState = captor.getAllValues().getLast();
        assertThat(finalState.getEstadoPdf()).isEqualTo(EstadoFacturaPdf.ERROR_FINAL);
        assertThat(finalState.getIntentosGeneracion()).isEqualTo(3);
        assertThat(finalState.getProximoReintento()).isNull();
    }

    private Pedido pedidoCompleto() {
        return Pedido.builder()
                .id(44L)
                .fecha(LocalDateTime.of(2026, 7, 3, 18, 30))
                .total(450.0)
                .domicilioEntrega(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                .medioDePago("Tarjeta")
                .local(Local.builder()
                        .id(10L)
                        .nombre("La Cocina")
                        .email("local@test.com")
                        .build())
                .cliente(Cliente.builder()
                        .id(20L)
                        .email("ana@test.com")
                        .nombre("Ana")
                        .apellido("Perez")
                        .build())
                .build();
    }

    private DetallePedido detallePedido(Pedido pedido) {
        return DetallePedido.builder()
                .cantidad(2)
                .precioUnitario(15.0)
                .subtotal(30.0)
                .plato(Plato.builder().id(100L).nombre("Milanesa").build())
                .pedido(pedido)
                .build();
    }

    private Factura facturaPendienteConSnapshot() {
        Pedido pedido = pedidoCompleto();
        return Factura.builder()
                .id(500L)
                .numero("FAC-44")
                .montoTotal(450.0)
                .estadoPdf(EstadoFacturaPdf.PENDIENTE)
                .intentosGeneracion(0)
                .localNombreSnapshot("La Cocina")
                .localEmailSnapshot("local@test.com")
                .clienteNombreSnapshot("Ana Perez")
                .clienteEmailSnapshot("ana@test.com")
                .direccionEntregaSnapshot("Av. Italia, 1234, Montevideo, 11600")
                .medioPagoSnapshot("Tarjeta")
                .detalles(List.of(FacturaDetalle.builder()
                        .nombreProductoSnapshot("Milanesa")
                        .cantidad(2)
                        .precioUnitario(15.0)
                        .subtotal(30.0)
                        .build()))
                .pedido(pedido)
                .build();
    }
}
