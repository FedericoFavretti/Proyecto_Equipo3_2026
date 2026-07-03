package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.DataTypes.response.DtCalificacionGlobalResponse;
import com.example.demo.Logica.DataTypes.response.DtMiCalificacionLocalResponse;
import com.example.demo.Logica.DataTypes.shared.DtCalificacion;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

public interface iCalificacionController {
    ResponseEntity<Void> calificar(@RequestBody DtCalificacion dtCalificacion, Authentication authentication);
    ResponseEntity<Map<String, Object>> consultarCalificacionGlobalDelLocal(Authentication authentication);
    ResponseEntity<DtCalificacionGlobalResponse> consultarCalificacionGlobal(@PathVariable("idCliente") Long idCliente);
    ResponseEntity<DtMiCalificacionLocalResponse> consultarMiCalificacionDeLocal(@PathVariable("idLocal") Long idLocal, Authentication authentication);

}
