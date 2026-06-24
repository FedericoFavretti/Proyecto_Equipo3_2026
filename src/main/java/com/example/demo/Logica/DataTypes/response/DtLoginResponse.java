package com.example.demo.Logica.DataTypes.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtLoginResponse {
    private Long id;
    private String token;
    private String tipo;
    private String email;
}

