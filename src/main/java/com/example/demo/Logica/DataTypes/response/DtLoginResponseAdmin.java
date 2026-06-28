package com.example.demo.Logica.DataTypes.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtLoginResponseAdmin extends DtLoginResponse {
    private String nivelAcceso;
}
