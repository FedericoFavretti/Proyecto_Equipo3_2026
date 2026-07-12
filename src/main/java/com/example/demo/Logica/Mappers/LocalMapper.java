package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.response.DtLocalBusquedaResponse;
import com.example.demo.Logica.DataTypes.response.DtLocalPerfilResponse;
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
                .celular(dtLocal.getCelular())
                .nombre(dtLocal.getNombre())
                .direccion(dtLocal.getDireccion())
                .descripcion(dtLocal.getDescripcion())
                .estadoLocal(dtLocal.getEstadoLocal())
                .calificacionGlobal(dtLocal.getCalificacionGlobal())
                .estaAbierto(dtLocal.getEstaAbierto())
                .imagenes(dtLocal.getImagenes())
                .telefonoFijo(dtLocal.getTelefonoFijo())
                .build();
    }

    public DtLocal mapearDtLocalDeClase(Local local){
        return DtLocal.builder()
                .id(local.getId())
                .email(local.getEmail())
                .foto(local.getFoto())
                .estadoCuenta(local.getEstado())
                .tipo(local.getTipo())
                .celular(local.getCelular())
                .nombre(local.getNombre())
                .direccion(local.getDireccion())
                .descripcion(local.getDescripcion())
                .estadoLocal(local.getEstadoLocal())
                .calificacionGlobal(local.getCalificacionGlobal())
                .estaAbierto(local.getEstaAbierto())
                .imagenes(local.getImagenes())
                .telefonoFijo(local.getTelefonoFijo())
                .build();
    }

    public DtLocalBusquedaResponse mapearDtLocalBusquedaDeClase(Local local) {
        return DtLocalBusquedaResponse.builder()
                .id(local.getId())
                .nombre(local.getNombre())
                .foto(local.getFoto())
                .direccion(local.getDireccion())
                .descripcion(local.getDescripcion())
                .calificacionGlobal(local.getCalificacionGlobal())
                .estaAbierto(local.getEstaAbierto())
                .imagenes(local.getImagenes())
                .build();
    }

    public DtLocalPerfilResponse mapearDtLocalPerfilDeClase(Local local) {
        return DtLocalPerfilResponse.builder()
                .id(local.getId())
                .nombre(local.getNombre())
                .foto(local.getFoto())
                .direccion(local.getDireccion())
                .descripcion(local.getDescripcion())
                .calificacionGlobal(local.getCalificacionGlobal())
                .estaAbierto(local.getEstaAbierto())
                .imagenes(local.getImagenes())
                .telefonoFijo(local.getTelefonoFijo())
                .celular(local.getCelular())
                .build();
    }
}

