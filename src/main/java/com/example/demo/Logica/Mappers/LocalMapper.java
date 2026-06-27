package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.response.DtLocalBusquedaResponse;
import org.springframework.stereotype.Component;

@Component
public class LocalMapper {

    public Local mapearLocalDeDt(DtLocal dtLocal){
        return Local.builder()
                .id(dtLocal.getId())
                .email(dtLocal.getEmail())
                .estado(dtLocal.getEstadoCuenta())
                .tipo(dtLocal.getTipo())
                .passwd(dtLocal.getPasswd())
                .foto(dtLocal.getFoto())
                .nombre(dtLocal.getNombre())
                .direccion(dtLocal.getDireccion())
                .descripcion(dtLocal.getDescripcion())
                .imagenes(dtLocal.getImagenes())
                .build();
    }

    public DtLocal mapearDtLocalDeClase(Local local){
        return DtLocal.builder()
                .id(local.getId())
                .email(local.getEmail())
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

    public DtLocalBusquedaResponse mapearDtLocalBusquedaDeClase(Local local) {
        return DtLocalBusquedaResponse.builder()
                .id(local.getId())
                .nombre(local.getNombre())
                .direccion(local.getDireccion())
                .descripcion(local.getDescripcion())
                .calificacionGlobal(local.getCalificacionGlobal())
                .estaAbierto(local.getEstaAbierto())
                .imagenes(local.getImagenes())
                .build();
    }
}

