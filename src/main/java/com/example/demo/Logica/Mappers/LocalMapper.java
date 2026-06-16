package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.DtLocal;
import org.springframework.stereotype.Component;
import com.example.demo.Logica.DataTypes.shared.DtLocal;

@Component
public class LocalMapper {

    public Local mapearLocalDeDt(DtLocal dtLocal){
        return Local.builder()
                .id(dtLocal.getId())
                .email(dtLocal.getEmail())
                .passwd(dtLocal.getPasswd())
                .estado(dtLocal.getEstadoCuenta())
                .tipo(dtLocal.getTipo())
                .nombre(dtLocal.getNombre())
                .direccion(dtLocal.getDireccion())
                .descripcion(dtLocal.getDescripcion())
                .estadoLocal(dtLocal.getEstadoLocal())
                .calificacionGlobal(dtLocal.getCalificacionGlobal())
                .estaAbierto(dtLocal.getEstaAbierto())
                .imagenes(dtLocal.getImagenes())
                .build();
    }

    public DtLocal mapearDtLocalDeClase(Local local){
        return DtLocal.builder()
                .id(local.getId())
                .email(local.getEmail())
                .passwd(local.getPasswd())
                .estadoCuenta(local.getEstado())
                .tipo(local.getTipo())
                .nombre(local.getNombre())
                .direccion(local.getDireccion())
                .descripcion(local.getDescripcion())
                .estadoLocal(local.getEstadoLocal())
                .calificacionGlobal(local.getCalificacionGlobal())
                .estaAbierto(local.getEstaAbierto())
                .imagenes(local.getImagenes())
                .build();
    }
}
