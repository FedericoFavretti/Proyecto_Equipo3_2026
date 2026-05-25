package com.example.demo.Logica.Controllers;
import com.example.demo.Logica.DataTypes.DtLoginRequest;
import com.example.demo.Logica.DataTypes.DtLoginResponse;
import com.example.demo.Logica.Interfaces.iUsuarioController;
import com.example.demo.Logica.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController implements iUsuarioController {
    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("")
    public ResponseEntity<DtLoginResponse> login(@RequestBody DtLoginRequest dtLogin) {
        DtLoginResponse dtLoginResponse = usuarioService.login(dtLogin);
        return ResponseEntity.ok(dtLoginResponse);
    }
}
