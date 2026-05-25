package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.DtLocal;
import com.example.demo.Logica.DataTypes.DtPlato;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class LocalService {
    @Autowired
    private LocalRepositorio localRepositorio;
    @Autowired
    private PlatoRepositorio platoRepositorio;

    @Transactional
    public Plato altaPlato(DtPlato dtPlato) {
        if(platoRepositorio.buscarPorNombre(dtPlato.getNombre()).isPresent()){
            throw  new IllegalArgumentException("El nombre del plato ya existe.");
        }
        if(dtPlato.getNombre() == null || dtPlato.getNombre().isEmpty() || dtPlato.getDescripcion() == null || dtPlato.getDescripcion().isEmpty() || dtPlato.getPrecio() == 0.0 || dtPlato.getImagenes().isEmpty() || dtPlato.getDisponible() == null ) {
            throw  new IllegalArgumentException("Debe completar todos los datos del plato.");
        }
        Local local = localRepositorio.buscarPorId(dtPlato.getDtLocal().getId()).orElseThrow(() -> new RuntimeException("Local no encontrado"));
        Plato plato = Plato.builder().nombre(dtPlato.getNombre()).descripcion(dtPlato.getDescripcion()).precio(dtPlato.getPrecio()).imagenes(dtPlato.getImagenes()).disponible(dtPlato.getDisponible()).local(local).build();
       return platoRepositorio.guardar(plato);
    }

    @Transactional
    public Plato gestionarPlatoModificacion(DtPlato dtPlato) {
        if(platoRepositorio.buscarPorNombre(dtPlato.getNombre()).isPresent()){
            throw  new IllegalArgumentException("El nombre del plato ya existe.");
        }
        if(dtPlato.getNombre() == null || dtPlato.getNombre().isEmpty() || dtPlato.getDescripcion() == null || dtPlato.getDescripcion().isEmpty() || dtPlato.getPrecio() == 0.0 || dtPlato.getImagenes().isEmpty() || dtPlato.getDisponible() == null ) {
            throw  new IllegalArgumentException("Debe modificar un dato para poder actualizar el plato.");
        }
        Local local = localRepositorio.buscarPorId(dtPlato.getDtLocal().getId()).orElseThrow(() -> new RuntimeException("Local no encontrado"));
        Plato plato = Plato.builder().nombre(dtPlato.getNombre()).descripcion(dtPlato.getDescripcion()).precio(dtPlato.getPrecio()).imagenes(dtPlato.getImagenes()).disponible(dtPlato.getDisponible()).local(local).build();
        return platoRepositorio.actualizar(plato);
    }

    @Transactional
    public void gestionarPlatoBaja(long idPlato) {
        platoRepositorio.eliminar(idPlato);
    }

    @Transactional
    public void solicitarHabilitacion(DtLocal dtLocal){

    }

    @Transactional
    public void registrarApertura(long idLocal){
        Local local = localRepositorio.buscarPorId(idLocal).orElseThrow(() -> new RuntimeException("Local no encontrado"));
        local.setEstaAbierto(true);
        localRepositorio.actualizar(local);
    }

    @Transactional
    public void regitrarCierre(long idLocal){
        Local local = localRepositorio.buscarPorId(idLocal).orElseThrow(() -> new RuntimeException("Local no encontrado"));
        local.setEstaAbierto(false);
        localRepositorio.actualizar(local);
    }
}
