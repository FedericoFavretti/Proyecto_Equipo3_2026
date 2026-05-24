package com.example.demo.Logica.Clases;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Administrador extends Usuario {
    private String nivelAcceso;
}
