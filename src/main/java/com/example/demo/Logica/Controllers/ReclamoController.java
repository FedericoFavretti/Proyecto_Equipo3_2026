package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Interfaces.iReclamoController;
import com.example.demo.Logica.Service.ReclamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reclamos")
public class ReclamoController implements iReclamoController {
    private final ReclamoService reclamoService;

    public ReclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }
}
