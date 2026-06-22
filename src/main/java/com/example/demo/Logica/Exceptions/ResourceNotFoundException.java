package com.example.demo.Logica.Exceptions;


public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String recurso, Object id) {
        super(recurso + " no encontrado con id: " + id);
    }
}
