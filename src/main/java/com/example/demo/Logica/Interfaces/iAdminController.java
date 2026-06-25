package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.DataTypes.request.DtResolverSolicitudLocalRequest;
import com.example.demo.Logica.DataTypes.response.DtSolicitudLocalPendienteResponse;
import com.example.demo.Logica.DataTypes.request.DtFiltroUsuario;
import com.example.demo.Logica.DataTypes.response.DtUsuarioListadoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

public interface iAdminController {
    ResponseEntity<List<DtSolicitudLocalPendienteResponse>> listarSolicitudesPendientes();
    ResponseEntity<Void> resolverSolicitud(@RequestBody DtResolverSolicitudLocalRequest request);
    ResponseEntity<List<DtUsuarioListadoResponse>> buscarYListarUsuarios(@RequestBody DtFiltroUsuario  dtFiltroUsuario);
}

