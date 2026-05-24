package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Interfaces.iReclamoController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reclamos")
public class ReclamoController implements iReclamoController {
}
