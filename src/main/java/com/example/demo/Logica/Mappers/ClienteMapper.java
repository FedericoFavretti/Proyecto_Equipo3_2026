package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.DataTypes.shared.DtCliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente mapearClienteDeDt(DtCliente dtCliente) {
        return Cliente.builder()
                .id(dtCliente.getId())
                .email(dtCliente.getEmail())
                .passwd(dtCliente.getPasswd())
                .foto(dtCliente.getFoto())
                .estado(dtCliente.getEstadoCuenta())
                .tipo(dtCliente.getTipo())
                .celular(dtCliente.getCelular())
                .documento(dtCliente.getDocumento())
                .nombre(dtCliente.getNombre())
                .apellido(dtCliente.getApellido())
                .direccion(dtCliente.getDireccion())
                .calificacionGlobal(dtCliente.getCalificacionGlobal())
                .activo(dtCliente.getActivo())
                .build();
    }

    public DtCliente mapearDtClienteDeClase(Cliente cliente) {
        return DtCliente.builder()
                .id(cliente.getId())
                .email(cliente.getEmail())
                .foto(cliente.getFoto())
                .estadoCuenta(cliente.getEstado())
                .tipo(cliente.getTipo())
                .celular(cliente.getCelular())
                .documento(cliente.getDocumento())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .direccion(cliente.getDireccion())
                .calificacionGlobal(cliente.getCalificacionGlobal())
                .activo(cliente.getActivo())
                .build();
    }
}

