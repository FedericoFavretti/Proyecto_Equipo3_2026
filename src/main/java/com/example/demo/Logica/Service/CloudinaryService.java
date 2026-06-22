package com.example.demo.Logica.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.Logica.Exceptions.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CloudinaryService {
    private static final String MENSAJE_ERROR_SUBIDA = "Error al subir la imagen al servicio de almacenamiento.";

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key",    apiKey,
                "api_secret", apiSecret
        ));
    }

    public String subirImagen(MultipartFile archivo) {
        try {
            Map resultado = cloudinary.uploader().upload(
                    archivo.getBytes(),
                    ObjectUtils.emptyMap()
            );
            return (String) resultado.get("secure_url");
        } catch (IOException e) {
            throw new ExternalServiceException(MENSAJE_ERROR_SUBIDA, e);
        }
    }

    public List<String> subirImagenes(List<MultipartFile> archivos) {
        return archivos.stream()
                .map(this::subirImagen)
                .collect(Collectors.toList());
    }
}