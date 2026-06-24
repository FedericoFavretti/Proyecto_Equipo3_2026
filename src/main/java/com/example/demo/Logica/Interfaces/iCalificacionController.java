package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.DataTypes.response.DtCalificacionGlobalResponse;
import com.example.demo.Logica.DataTypes.shared.DtCalificacion;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

public interface iCalificacionController {
    ResponseEntity<Void> calificar(@RequestBody DtCalificacion dtCalificacion);
    ResponseEntity<Map<String, Object>> consultarCalificacionGlobalDelLocal(Authentication authentication);
    public ResponseEntity<DtCalificacionGlobalResponse> consultarCalificacionGlobal(@PathVariable("idCliente") Long idCliente);
}
