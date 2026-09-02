package com.gamestore.catalogo.config;

import com.gamestore.catalogo.model.Juego;
import com.gamestore.catalogo.model.Seccion;
import com.gamestore.catalogo.repository.JuegoRepository;
import com.gamestore.catalogo.repository.SeccionRepository;
import com.gamestore.catalogo.service.GeneradorPortadas;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * Datos de ejemplo del catalogo. Igual que en el monolito, pero el vendedor
 * de cada juego es un {@code username} (que existe como perfil en
 * usuarios-service gracias a su propio DataInitializer).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final SeccionRepository seccionRepository;
    private final JuegoRepository juegoRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private static final Color[][] PALETA = {
            {new Color(29, 32, 92), new Color(111, 66, 193)},
            {new Color(92, 30, 20), new Color(191, 130, 30)},
            {new Color(15, 84, 74), new Color(46, 204, 113)},
            {new Color(15, 76, 90), new Color(52, 152, 219)},
            {new Color(58, 58, 58), new Color(230, 126, 34)},
    };

    public DataInitializer(SeccionRepository seccionRepository, JuegoRepository juegoRepository) {
        this.seccionRepository = seccionRepository;
        this.juegoRepository = juegoRepository;
    }

    @Override
    public void run(String... args) {
        repararImagenesFaltantes();

        if (juegoRepository.count() > 0 || seccionRepository.count() > 0) {
            return;
        }

        limpiarImagenesViejas();

        Seccion accion = seccionRepository.save(new Seccion("Accion", "Juegos rapidos, disparos y combate"));
        Seccion rpg = seccionRepository.save(new Seccion("RPG", "Juegos de rol con historia y progresion"));
        Seccion deportes = seccionRepository.save(new Seccion("Deportes", "Simuladores deportivos"));
        Seccion estrategia = seccionRepository.save(new Seccion("Estrategia", "Juegos de gestion y tactica"));

        Path dir = Paths.get(uploadDir);

        guardarJuego("Galaxy Raiders", "Shooter espacial cooperativo", "39.99", accion, "vendedor1",
                GeneradorPortadas.generar(dir, "Galaxy Raiders", "Accion", new Color(29, 32, 92), new Color(111, 66, 193)));
        guardarJuego("Reinos Perdidos", "RPG de mundo abierto con crafting", "59.99", rpg, "vendedor1",
                GeneradorPortadas.generar(dir, "Reinos Perdidos", "RPG", new Color(92, 30, 20), new Color(191, 130, 30)));
        guardarJuego("Pixel Knights", "Aventura RPG retro en pixel art", "19.99", rpg, "vendedor2",
                GeneradorPortadas.generar(dir, "Pixel Knights", "RPG", new Color(15, 84, 74), new Color(46, 204, 113)));
        guardarJuego("Turbo League", "Carreras arcade de futbol con autos", "29.99", deportes, "vendedor2",
                GeneradorPortadas.generar(dir, "Turbo League", "Deportes", new Color(15, 76, 90), new Color(52, 152, 219)));
        guardarJuego("Imperios de Acero", "Estrategia por turnos de construccion de imperios", "34.99", estrategia, "vendedor1",
                GeneradorPortadas.generar(dir, "Imperios de Acero", "Estrategia", new Color(58, 58, 58), new Color(230, 126, 34)));

        System.out.println("[catalogo-service] catalogo de prueba cargado (4 secciones, 5 juegos)");
    }

    private void guardarJuego(String nombre, String descripcion, String precio, Seccion seccion,
                              String vendedorUsername, String imagenUrl) {
        Juego juego = new Juego();
        juego.setNombre(nombre);
        juego.setDescripcion(descripcion);
        juego.setPrecio(new BigDecimal(precio));
        juego.setSeccion(seccion);
        juego.setVendedorUsername(vendedorUsername);
        juego.setImagenUrl(imagenUrl);
        juegoRepository.save(juego);
    }

    private void repararImagenesFaltantes() {
        Path dir = Paths.get(uploadDir);
        int reparadas = 0;
        for (Juego juego : juegoRepository.findAll()) {
            String url = juego.getImagenUrl();
            if (url == null || url.isBlank()) {
                continue;
            }
            String nombreArchivo = url.substring(url.lastIndexOf('/') + 1);
            if (Files.exists(dir.resolve(nombreArchivo))) {
                continue;
            }
            String etiqueta = juego.getSeccion() != null ? juego.getSeccion().getNombre() : "Juego";
            Color[] colores = PALETA[Math.floorMod(juego.getId().hashCode(), PALETA.length)];
            String nuevaUrl = GeneradorPortadas.generar(dir, juego.getNombre(), etiqueta, colores[0], colores[1]);
            if (nuevaUrl != null) {
                juego.setImagenUrl(nuevaUrl);
                juegoRepository.save(juego);
                reparadas++;
            }
        }
        if (reparadas > 0) {
            System.out.println("[catalogo-service] se regeneraron " + reparadas + " portada(s) que faltaban en disco.");
        }
    }

    private void limpiarImagenesViejas() {
        Path dir = Paths.get(uploadDir);
        if (!Files.isDirectory(dir)) {
            return;
        }
        try (var archivos = Files.list(dir)) {
            archivos.filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().equals(".gitkeep"))
                    .sorted(Comparator.naturalOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException ignored) {
                            // best-effort
                        }
                    });
        } catch (IOException ignored) {
            // idem
        }
    }
}
