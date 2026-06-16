package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.DataTypes.shared.DtCalificacion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface iCalificacionController {
    public ResponseEntity<Void> calificar(@RequestBody DtCalificacion dtCalificacion);
}
