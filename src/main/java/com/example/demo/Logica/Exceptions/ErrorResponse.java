package com.example.demo.Logica.Exceptions;

import java.time.LocalDateTime;

public record ErrorResponse(
        String mensaje,
        int status,
        LocalDateTime timestamp,
        String path
) {}