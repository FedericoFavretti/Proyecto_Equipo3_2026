package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Interfaces.iCalificacionController;
import com.example.demo.Logica.Service.CalificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/calificaciones")
public class CalificacionController implements iCalificacionController {
    @Autowired
    private CalificacionService calificacionService;

}
