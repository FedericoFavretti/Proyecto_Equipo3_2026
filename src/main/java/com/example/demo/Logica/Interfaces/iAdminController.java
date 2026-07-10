package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.DataTypes.request.DtResolverSolicitudLocalRequest;
import com.example.demo.Logica.DataTypes.response.DtSolicitudLocalPendienteResponse;
import com.example.demo.Logica.DataTypes.response.DtUsuarioListadoResponse;
import com.example.demo.Logica.DataTypes.response.DtPagina;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import com.example.demo.Logica.Enums.EstadoCuenta;

public interface iAdminController {
    ResponseEntity<List<DtSolicitudLocalPendienteResponse>> listarSolicitudesPendientes();
    ResponseEntity<Void> resolverSolicitud(@RequestBody DtResolverSolicitudLocalRequest request);
    ResponseEntity<DtPagina<DtUsuarioListadoResponse>> buscarYListarUsuarios(
            String texto, String tipoUsuario, EstadoCuenta estado, String ordenarPor, String direccion,
            Integer pagina, Integer tamanio);
}

