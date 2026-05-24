package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Interfaces.iAdminController;
import com.example.demo.Logica.Interfaces.iCalificacionController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/calificaciones")
public class CalificacionController implements iCalificacionController {
}
