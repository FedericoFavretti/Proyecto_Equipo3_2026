package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.Clases.FacturaDetalle;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class FacturaPdfGeneratorService {

    private static final Charset PDF_CHARSET = StandardCharsets.ISO_8859_1;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int MAX_LINEAS_POR_PAGINA = 34;

    public byte[] generarFacturaPdf(Factura factura, List<FacturaDetalle> detalles) {
        List<String> lineas = construirLineasFactura(factura, detalles);
        List<List<String>> paginas = paginar(lineas, MAX_LINEAS_POR_PAGINA);
        return construirPdf(paginas);
    }

    private List<String> construirLineasFactura(Factura factura, List<FacturaDetalle> detalles) {
        List<String> lineas = new ArrayList<>();
        lineas.add("Factura " + valorSeguro(factura.getNumero()));
        lineas.add("Pedido: #" + (factura.getPedido() != null ? factura.getPedido().getId() : "-"));
        lineas.add("Fecha pedido: " + (factura.getFechaPedido() != null
                ? factura.getFechaPedido().format(DATE_TIME_FORMATTER)
                : "-"));
        lineas.add("Fecha emision: " + (factura.getFechaEmision() != null
                ? factura.getFechaEmision().format(DATE_TIME_FORMATTER)
                : factura.getFechaUltimoIntento() != null
                ? factura.getFechaUltimoIntento().format(DATE_TIME_FORMATTER)
                : "-"));
        lineas.add("");
        lineas.add("Local: " + valorSeguro(factura.getLocalNombreSnapshot()));
        lineas.add("Email local: " + valorSeguro(factura.getLocalEmailSnapshot()));
        lineas.add("Cliente: " + valorSeguro(factura.getClienteNombreSnapshot()));
        lineas.add("Email cliente: " + valorSeguro(factura.getClienteEmailSnapshot()));
        lineas.add("Direccion entrega: " + valorSeguro(factura.getDireccionEntregaSnapshot()));
        lineas.add("Medio de pago: " + valorSeguro(factura.getMedioPagoSnapshot()));
        lineas.add(String.format(Locale.US, "Total: %.2f", factura.getMontoTotal() != null ? factura.getMontoTotal() : 0.0));
        lineas.add("");
        lineas.add("Detalle:");

        if (detalles == null || detalles.isEmpty()) {
            lineas.add("- Sin items registrados");
        } else {
            for (FacturaDetalle detalle : detalles) {
                lineas.add("- " + valorSeguro(detalle.getNombreProductoSnapshot()));
                lineas.add("  Cantidad: " + valorSeguro(detalle.getCantidad()));
                lineas.add(String.format(Locale.US, "  Precio unitario: %.2f", detalle.getPrecioUnitario() != null ? detalle.getPrecioUnitario() : 0.0));
                lineas.add(String.format(Locale.US, "  Subtotal: %.2f", detalle.getSubtotal() != null ? detalle.getSubtotal() : 0.0));
            }
        }

        return lineas;
    }

    private List<List<String>> paginar(List<String> lineas, int maxLineasPorPagina) {
        List<List<String>> paginas = new ArrayList<>();
        for (int inicio = 0; inicio < lineas.size(); inicio += maxLineasPorPagina) {
            int fin = Math.min(inicio + maxLineasPorPagina, lineas.size());
            paginas.add(new ArrayList<>(lineas.subList(inicio, fin)));
        }
        return paginas;
    }

    private byte[] construirPdf(List<List<String>> paginas) {
        int fontObjectNumber = 3 + (paginas.size() * 2);
        List<byte[]> objetos = new ArrayList<>();

        objetos.add(bytes("<< /Type /Catalog /Pages 2 0 R >>"));

        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < paginas.size(); i++) {
            int pageObjectNumber = 3 + (i * 2);
            if (kids.length() > 0) {
                kids.append(' ');
            }
            kids.append(pageObjectNumber).append(" 0 R");
        }
        objetos.add(bytes("<< /Type /Pages /Kids [" + kids + "] /Count " + paginas.size() + " >>"));

        for (int i = 0; i < paginas.size(); i++) {
            int contentObjectNumber = 4 + (i * 2);
            objetos.add(bytes("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 " + fontObjectNumber + " 0 R >> >> /Contents " + contentObjectNumber + " 0 R >>"));

            String stream = renderizarContenidoPagina(paginas.get(i));
            byte[] streamBytes = bytes(stream);
            String encabezado = "<< /Length " + streamBytes.length + " >>\nstream\n";
            String cierre = "\nendstream";
            objetos.add(bytes(encabezado + stream + cierre));
        }

        objetos.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"));

        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);

        for (int i = 0; i < objetos.size(); i++) {
            offsets.add(pdf.toString().getBytes(PDF_CHARSET).length);
            pdf.append(i + 1).append(" 0 obj\n");
            pdf.append(new String(objetos.get(i), PDF_CHARSET));
            pdf.append("\nendobj\n");
        }

        int startxref = pdf.toString().getBytes(PDF_CHARSET).length;
        pdf.append("xref\n");
        pdf.append("0 ").append(objetos.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");

        for (int i = 1; i < offsets.size(); i++) {
            pdf.append(String.format(Locale.US, "%010d 00000 n %n", offsets.get(i)));
        }

        pdf.append("trailer\n");
        pdf.append("<< /Size ").append(objetos.size() + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n");
        pdf.append(startxref).append("\n");
        pdf.append("%%EOF");

        return bytes(pdf.toString());
    }

    private String renderizarContenidoPagina(List<String> lineas) {
        StringBuilder contenido = new StringBuilder();
        contenido.append("BT\n");
        contenido.append("/F1 12 Tf\n");
        contenido.append("50 760 Td\n");

        boolean primera = true;
        for (String linea : lineas) {
            if (!primera) {
                contenido.append("0 -18 Td\n");
            }
            contenido.append("(").append(escaparPdf(linea)).append(") Tj\n");
            primera = false;
        }

        contenido.append("ET");
        return contenido.toString();
    }

    private String escaparPdf(String valor) {
        return valor
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("\r", " ")
                .replace("\n", " ");
    }

    private String valorSeguro(Object valor) {
        return valor == null ? "-" : valor.toString();
    }

    private byte[] bytes(String valor) {
        return valor.getBytes(PDF_CHARSET);
    }
}
