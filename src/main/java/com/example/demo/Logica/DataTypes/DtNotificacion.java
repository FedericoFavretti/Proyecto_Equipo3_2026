package com.example.demo.Logica.DataTypes;
import com.example.demo.Logica.Enums.CanalNotificacion;
import com.example.demo.Logica.Enums.TipoNotificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtNotificacion {
    private long id;
    private TipoNotificacion tipo;
    private String mensaje;
    private CanalNotificacion canal;
    private Boolean leida;
    private Date fecha;
}
