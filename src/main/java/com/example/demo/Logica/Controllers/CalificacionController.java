package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.shared.DtCalificacion;
import com.example.demo.Logica.Interfaces.iCalificacionController;
import com.example.demo.Logica.Service.CalificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/calificaciones")
public class CalificacionController implements iCalificacionController {

    private final CalificacionService calificacionService;

    public CalificacionController(CalificacionService calificacionService) {
        this.calificacionService = calificacionService;
    }

    @PutMapping("/calificar")
    public ResponseEntity<Void> calificar(@RequestBody DtCalificacion dtCalificacion){
        calificacionService.calificar(dtCalificacion);
        return ResponseEntity.noContent().build();
    }

}
