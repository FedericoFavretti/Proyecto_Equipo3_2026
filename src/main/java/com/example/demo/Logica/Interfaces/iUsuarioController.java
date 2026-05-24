package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.DataTypes.DtLoginRequest;
import com.example.demo.Logica.DataTypes.DtLoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface iUsuarioController {
    ResponseEntity<DtLoginResponse> login(@RequestBody DtLoginRequest dtLogin);
}
