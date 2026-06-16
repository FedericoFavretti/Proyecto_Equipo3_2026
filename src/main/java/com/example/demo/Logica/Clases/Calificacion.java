package com.example.demo.Logica.Clases;
import com.example.demo.Logica.Enums.TipoCalificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Calificacion {
    private Long id;
    private int puntaje;
    private String comentario;
    private LocalDateTime  fecha;
    private TipoCalificacion tipo;
    private Cliente cliente;
    private Local local;
}
