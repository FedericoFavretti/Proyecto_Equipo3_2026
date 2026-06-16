package com.example.demo.Logica.DataTypes;

import com.example.demo.Logica.Enums.EstadoLocal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtResloverHabilitacionLocal {
    private Long id;
    private EstadoLocal estado;
}
