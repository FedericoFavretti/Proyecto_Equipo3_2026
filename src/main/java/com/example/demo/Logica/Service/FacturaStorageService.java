package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Factura;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FacturaStorageService {

    private final Path storageDir;

    public FacturaStorageService(@Value("${app.facturas.storage-dir:facturas}") String storageDir) {
        this.storageDir = Paths.get(storageDir);
    }

    public String guardarFacturaPdf(Factura factura, byte[] contenidoPdf) throws IOException {
        Files.createDirectories(storageDir);

        String nombreArchivo = construirNombreArchivo(factura);
        Path rutaArchivo = storageDir.resolve(nombreArchivo);
        Files.write(rutaArchivo, contenidoPdf);

        return rutaArchivo.toString().replace("\\", "/");
    }

    private String construirNombreArchivo(Factura factura) {
        String numero = factura.getNumero() == null ? "factura" : factura.getNumero().replaceAll("[^a-zA-Z0-9-_]", "_");
        Long pedidoId = factura.getPedido() != null ? factura.getPedido().getId() : null;
        String sufijoPedido = pedidoId != null ? "-pedido-" + pedidoId : "";
        return numero + sufijoPedido + ".pdf";
    }
}
