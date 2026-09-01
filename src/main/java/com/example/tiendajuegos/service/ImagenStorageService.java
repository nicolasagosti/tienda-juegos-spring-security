package com.example.tiendajuegos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Guarda en disco la imagen que sube el VENDEDOR al publicar un juego y
 * devuelve la URL publica (servida por WebConfig) para guardarla en la
 * entidad Juego.
 */
@Service
public class ImagenStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }
        try {
            Path carpeta = Paths.get(uploadDir);
            Files.createDirectories(carpeta);

            String nombreOriginal = StringUtils.cleanPath(archivo.getOriginalFilename() != null
                    ? archivo.getOriginalFilename() : "imagen");
            String extension = "";
            int idx = nombreOriginal.lastIndexOf('.');
            if (idx >= 0) {
                extension = nombreOriginal.substring(idx);
            }
            String nombreArchivo = UUID.randomUUID() + extension;

            Path destino = carpeta.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + nombreArchivo;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar la imagen del juego", e);
        }
    }
}
