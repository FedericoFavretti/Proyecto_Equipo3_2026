package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.Clases.FacturaDetalle;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class FacturaPdfGeneratorService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String LOGO_URL = "https://res.cloudinary.com/dh8f9uvlu/image/upload/v1783563076/foodly_sycini.png";
    private static final String SITIO_WEB = "https://frontend-proyecto-foodly-test.up.railway.app/";
    private static final String ICONO_PEDIDO = "https://res.cloudinary.com/dh8f9uvlu/image/upload/v1783565201/WhatsApp_Image_2026-07-08_at_11.44.02_PM_1_a0zrqh.jpg";
    private static final String ICONO_CLIENTE = "https://res.cloudinary.com/dh8f9uvlu/image/upload/v1783565217/WhatsApp_Image_2026-07-08_at_11.44.03_PM_1_fyjam2.jpg";
    private static final String ICONO_DIRECCION = "https://res.cloudinary.com/dh8f9uvlu/image/upload/v1783565191/WhatsApp_Image_2026-07-08_at_11.44.02_PM_busy9b.jpg";
    private static final String ICONO_MEDIO_PAGO = "https://res.cloudinary.com/dh8f9uvlu/image/upload/v1783565209/WhatsApp_Image_2026-07-08_at_11.44.03_PM_qsds2s.jpg";
    private static final String LOGO_LOCAL_POR_DEFECTO = "https://res.cloudinary.com/dh8f9uvlu/image/upload/v1783636365/foodly_sycini_x7k5rh.png";

    public byte[] generarFacturaPdf(Factura factura, List<FacturaDetalle> detalles) {
        String html = construirHtmlFactura(factura, detalles);
        return renderizarPdf(html);
    }

    private byte[] renderizarPdf(String html) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(salida);
            builder.run();
            return salida.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando el PDF de la factura", e);
        }
    }

    private String construirHtmlFactura(Factura factura, List<FacturaDetalle> detalles) {
        double subtotal = detalles == null ? 0.0 : detalles.stream()
                .mapToDouble(d -> d.getSubtotal() != null ? d.getSubtotal() : 0.0)
                .sum();
        double total = factura.getMontoTotal() != null ? factura.getMontoTotal() : subtotal;
        double envio = Math.max(0.0, total - subtotal);

        String fechaPedido = factura.getFechaPedido() != null
                ? factura.getFechaPedido().format(DATE_TIME_FORMATTER)
                : "-";
        String fechaEmision = factura.getFechaEmision() != null
                ? factura.getFechaEmision().format(DATE_TIME_FORMATTER)
                : factura.getFechaUltimoIntento() != null
                ? factura.getFechaUltimoIntento().format(DATE_TIME_FORMATTER)
                : "-";
        String pedidoId = factura.getPedido() != null ? String.valueOf(factura.getPedido().getId()) : "-";

        StringBuilder filas = new StringBuilder();
        if (detalles == null || detalles.isEmpty()) {
            filas.append("<tr><td colspan=\"4\" class=\"sin-items\">Sin items registrados</td></tr>");
        } else {
            for (FacturaDetalle d : detalles) {
                filas.append("<tr>")
                        .append("<td>").append(escapeHtml(valorSeguro(d.getNombreProductoSnapshot()))).append("</td>")
                        .append("<td class=\"centro\">").append(escapeHtml(valorSeguro(d.getCantidad()))).append("</td>")
                        .append("<td class=\"derecha\">").append(formatoMoneda(d.getPrecioUnitario())).append("</td>")
                        .append("<td class=\"derecha\">").append(formatoMoneda(d.getSubtotal())).append("</td>")
                        .append("</tr>");
            }
        }

        String plantilla = """
                <html>
                <head>
                <style>
                    @page { size: 620px 900px; margin: 0; }
                    * { box-sizing: border-box; }
                    body {
                        margin: 0; padding: 0;
                        font-family: Helvetica, Arial, sans-serif;
                        color: #1f2937;
                        background: #ffffff;
                    }
                    .hoja { padding: 28px 32px 0 32px; }
                    table { border-collapse: collapse; width: 100%; }
                    td, th { vertical-align: top; }

                    .logo-img { width: 70px; height: 70px; }
                    .tagline { margin-top: 8px; font-size: 12px; color: #6b7280; line-height: 1.5; }

                    .col-derecha { text-align: right; width: 55%; }
                    .factura-label { font-size: 13px; color: #374151; }
                    .factura-numero { font-size: 30px; font-weight: bold; color: #1565d8; margin: 2px 0 12px 0; }
                    .fecha-fila { font-size: 11px; color: #374151; margin-bottom: 5px; }
                    .fecha-fila b { color: #111827; }
                    .punto { display: inline-block; width: 7px; height: 7px; background: #f4a261; border-radius: 50%; margin-right: 6px; }

                    .caja-info { background: #fdf3ea; border-radius: 16px; margin-top: 26px; }
                    .caja-info td { padding: 18px 14px; }
                    .celda-info { width: 25%; border-right: 1px solid #f0dfcd; text-align: center; }
                    .celda-info:last-child { border-right: none; }
                    .circulo-icono { display: block; width: 28px; height: 28px; background: #e2edfb; border-radius: 50%; margin: 0 auto 10px auto; text-align: center; overflow: hidden; }
                    .circulo-icono img { width: 28px; height: 28px; }
                    .info-etiqueta { font-size: 10px; color: #6b7280; margin-bottom: 4px; }
                    .info-valor { font-size: 13px; font-weight: bold; color: #111827; }
                    .info-valor-chico { font-size: 10px; color: #6b7280; margin-top: 2px; }

                    .caja-local { border: 1px solid #eee; border-radius: 16px; margin-top: 20px; }
                    .caja-local td { padding: 16px 18px; vertical-align: middle; }
                    .circulo-local { display: block; width: 44px; height: 44px; background: #ffffff; border-radius: 50%; text-align: center; overflow: hidden; }
                    .circulo-local img { width: 44px; height: 44px; margin-top: 0; }
                    .local-etiqueta { font-size: 12px; color: #1565d8; font-weight: bold; }
                    .local-nombre { font-size: 17px; font-weight: bold; color: #111827; margin: 3px 0; }
                    .local-email { font-size: 11px; color: #6b7280; }

                    .titulo-seccion { font-size: 15px; font-weight: bold; color: #1565d8; margin-top: 28px; margin-bottom: 10px; }

                    .tabla-detalle { border: 1px solid #f0e2d2; border-radius: 10px; margin-top: 4px; }
                    .tabla-detalle th { background: #fbe8d3; text-align: left; padding: 10px 12px; font-size: 11px; color: #92603a; }
                    .tabla-detalle td { padding: 10px 12px; font-size: 12px; border-top: 1px solid #f2f2f2; }
                    .centro { text-align: center; }
                    .derecha { text-align: right; }
                    .sin-items { text-align: center; color: #9ca3af; padding: 16px; }

                    .fila-inferior { margin-top: 24px; }
                    .celda-gracias { width: 50%; padding-right: 12px; }
                    .celda-totales { width: 50%; padding-left: 12px; }

                    .caja-gracias { background: #eef4fb; border-radius: 14px; padding: 16px; }
                    .gracias-titulo { font-weight: bold; color: #1565d8; font-size: 13px; margin-bottom: 4px; }
                    .gracias-texto { font-size: 11px; color: #6b7280; line-height: 1.5; }
                    .gracias-web { font-size: 11px; color: #1565d8; margin-top: 10px; }

                    .tabla-totales td { padding: 8px 0; font-size: 12px; border-bottom: 1px solid #eee; }
                    .et-totales { color: #374151; }
                    .val-totales { text-align: right; font-weight: bold; color: #111827; }

                    .barra-total { background: #1565d8; border-radius: 10px; margin-top: 12px; }
                    .barra-total td { padding: 13px 16px; color: #ffffff; font-weight: bold; font-size: 16px; }

                    .pie { margin-top: 30px; background: #fdf3ea; padding: 20px; text-align: center; border-radius: 24px 24px 0 0; }
                    .pie-titulo { font-weight: bold; color: #111827; font-size: 13px; }
                    .pie-sub { font-size: 10px; color: #6b7280; margin-top: 3px; }
                </style>
                </head>
                <body>
                <div class="hoja">

                    <table>
                        <tr>
                            <td style="width:45%;">
                                <img class="logo-img" src="{{LOGO_URL}}" />
                                <div class="tagline">Miles de sabores.<br/>Un solo lugar.</div>
                            </td>
                            <td class="col-derecha">
                                <div class="factura-label">Factura</div>
                                <div class="factura-numero">{{NUMERO}}</div>
                                <div class="fecha-fila"><span class="punto"></span>Fecha del pedido: <b>{{FECHA_PEDIDO}}</b></div>
                                <div class="fecha-fila"><span class="punto"></span>Fecha de emisi\u00f3n: <b>{{FECHA_EMISION}}</b></div>
                            </td>
                        </tr>
                    </table>

                    <table class="caja-info">
                        <tr>
                            <td class="celda-info">
                                <span class="circulo-icono"><img src="{{ICONO_PEDIDO}}" /></span>
                                <div class="info-etiqueta">Pedido</div>
                                <div class="info-valor">#{{PEDIDO_ID}}</div>
                            </td>
                            <td class="celda-info">
                                <span class="circulo-icono"><img src="{{ICONO_CLIENTE}}" /></span>
                                <div class="info-etiqueta">Cliente</div>
                                <div class="info-valor">{{CLIENTE_NOMBRE}}</div>
                                <div class="info-valor-chico">{{CLIENTE_EMAIL}}</div>
                            </td>
                            <td class="celda-info">
                                <span class="circulo-icono"><img src="{{ICONO_DIRECCION}}" /></span>
                                <div class="info-etiqueta">Direcci\u00f3n de entrega</div>
                                <div class="info-valor">{{DIRECCION}}</div>
                            </td>
                            <td class="celda-info">
                                <span class="circulo-icono"><img src="{{ICONO_MEDIO_PAGO}}" /></span>
                                <div class="info-etiqueta">Medio de pago</div>
                                <div class="info-valor">{{MEDIO_PAGO}}</div>
                            </td>
                        </tr>
                    </table>

                    <table class="caja-local">
                        <tr>
                            <td style="width:60px;"><span class="circulo-local"><img src="{{LOGO_LOCAL}}" /></span></td>
                            <td>
                                <div class="local-etiqueta">Local</div>
                                <div class="local-nombre">{{LOCAL_NOMBRE}}</div>
                                <div class="local-email">{{LOCAL_EMAIL}}</div>
                            </td>
                        </tr>
                    </table>

                    <div class="titulo-seccion">Detalle del pedido</div>
                    <table class="tabla-detalle">
                        <tr>
                            <th>Producto</th>
                            <th class="centro">Cantidad</th>
                            <th class="derecha">Precio unitario</th>
                            <th class="derecha">Subtotal</th>
                        </tr>
                        {{FILAS_DETALLE}}
                    </table>

                    <table class="fila-inferior">
                        <tr>
                            <td class="celda-gracias">
                                <div class="caja-gracias">
                                    <div class="gracias-titulo">\u00a1Gracias por elegir Foodly!</div>
                                    <div class="gracias-texto">Nos alegra llevar tu comida favorita hasta donde est\u00e9s.</div>
                                    <div class="gracias-web">{{SITIO_WEB}}</div>
                                </div>
                            </td>
                            <td class="celda-totales">
                                <table class="tabla-totales">
                                    <tr><td class="et-totales">Subtotal</td><td class="val-totales">{{SUBTOTAL}}</td></tr>
                                    <tr><td class="et-totales">Costo de env\u00edo</td><td class="val-totales">{{ENVIO}}</td></tr>
                                </table>
                                <table class="barra-total">
                                    <tr><td>TOTAL</td><td class="derecha">{{TOTAL}}</td></tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <div class="pie">
                        <div class="pie-titulo">\u00a1Gracias por tu compra!</div>
                        <div class="pie-sub">Factura generada autom\u00e1ticamente</div>
                    </div>

                </div>
                </body>
                </html>
                """;

        return plantilla
                .replace("{{LOGO_URL}}", LOGO_URL)
                .replace("{{SITIO_WEB}}", SITIO_WEB)
                .replace("{{ICONO_PEDIDO}}", ICONO_PEDIDO)
                .replace("{{ICONO_CLIENTE}}", ICONO_CLIENTE)
                .replace("{{ICONO_DIRECCION}}", ICONO_DIRECCION)
                .replace("{{ICONO_MEDIO_PAGO}}", ICONO_MEDIO_PAGO)
                .replace("{{NUMERO}}", escapeHtml(valorSeguro(factura.getNumero())))
                .replace("{{FECHA_PEDIDO}}", fechaPedido)
                .replace("{{FECHA_EMISION}}", fechaEmision)
                .replace("{{PEDIDO_ID}}", escapeHtml(pedidoId))
                .replace("{{CLIENTE_NOMBRE}}", escapeHtml(valorSeguro(factura.getClienteNombreSnapshot())))
                .replace("{{CLIENTE_EMAIL}}", escapeHtml(valorSeguro(factura.getClienteEmailSnapshot())))
                .replace("{{DIRECCION}}", escapeHtml(valorSeguro(factura.getDireccionEntregaSnapshot())))
                .replace("{{MEDIO_PAGO}}", escapeHtml(valorSeguro(factura.getMedioPagoSnapshot())))
                .replace("{{LOCAL_NOMBRE}}", escapeHtml(valorSeguro(factura.getLocalNombreSnapshot())))
                .replace("{{LOCAL_EMAIL}}", escapeHtml(valorSeguro(factura.getLocalEmailSnapshot())))
                .replace("{{FILAS_DETALLE}}", filas.toString())
                .replace("{{SUBTOTAL}}", formatoMoneda(subtotal))
                .replace("{{ENVIO}}", formatoMoneda(envio))
                .replace("{{TOTAL}}", formatoMoneda(total))
                .replace("{{LOGO_LOCAL}}", valorSeguro(factura.getLocalLogoSnapshot(), LOGO_LOCAL_POR_DEFECTO));
    }

    private String formatoMoneda(Double valor) {
        double v = valor != null ? valor : 0.0;
        return String.format(Locale.US, "$%,.2f", v);
    }

    private String valorSeguro(Object valor) {
        return valor == null ? "-" : valor.toString();
    }

    private String valorSeguro(String valor, String porDefecto) {
        return (valor == null || valor.isBlank()) ? porDefecto : valor;
    }

    private String escapeHtml(String valor) {
        if (valor == null) {
            return "";
        }
        return valor
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}