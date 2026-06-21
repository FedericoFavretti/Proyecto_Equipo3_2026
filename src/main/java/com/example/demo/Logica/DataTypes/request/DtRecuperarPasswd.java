package com.example.demo.Logica.DataTypes.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtRecuperarPasswd {
    private String token;
    private String nuevaPasswd;
}