package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Notificacion;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.Enums.CanalNotificacion;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Enums.TipoNotificacion;
import com.example.demo.Persistencia.Repositorios.NotificacionRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificacionPedidoServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private NotificacionRepositorio notificacionRepositorio;

    @InjectMocks
    private NotificacionPedidoService notificacionPedidoService;

    @Test
    void notificarRechazoPersisteNotificacionYEnviaCorreo() {
        Pedido pedido = Pedido.builder()
                .id(44L)
                .fecha(LocalDateTime.now())
                .total(120.0)
                .domicilioEntrega(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                .medioDePago("Tarjeta")
                .pagoSimulado(false)
                .estado(EstadoPedido.Pendiente)
                .cliente(Cliente.builder()
                        .id(20L)
                        .email("ana@test.com")
                        .nombre("Ana")
                        .apellido("Pérez")
                        .activo(true)
                        .build())
                .build();

        notificacionPedidoService.notificarRechazo(pedido, "Sin disponibilidad");

        ArgumentCaptor<Notificacion> notificacionCaptor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepositorio).guardar(notificacionCaptor.capture());
        verify(emailService).enviarCorreo(
                "ana@test.com",
                "Pedido rechazado",
                "Tu pedido #44 fue rechazado. Motivo: Sin disponibilidad"
        );

        Notificacion notificacion = notificacionCaptor.getValue();
        assertThat(notificacion.getTipo()).isEqualTo(TipoNotificacion.Pedido);
        assertThat(notificacion.getCanal()).isEqualTo(CanalNotificacion.Email);
        assertThat(notificacion.getMensaje()).isEqualTo("Tu pedido #44 fue rechazado. Motivo: Sin disponibilidad");
        assertThat(notificacion.getPedido()).isSameAs(pedido);
        assertThat(notificacion.getLeida()).isFalse();
        assertThat(notificacion.getFecha()).isNotNull();
    }
}
