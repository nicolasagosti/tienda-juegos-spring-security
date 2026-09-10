package com.gamestore.negocio.catalogo.config;

import com.gamestore.negocio.catalogo.model.Compra;
import com.gamestore.negocio.catalogo.model.Juego;
import com.gamestore.negocio.catalogo.model.Seccion;
import com.gamestore.negocio.catalogo.repository.CompraRepository;
import com.gamestore.negocio.catalogo.repository.JuegoRepository;
import com.gamestore.negocio.catalogo.repository.SeccionRepository;
import com.gamestore.negocio.catalogo.service.GeneradorPortadas;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * Datos de ejemplo del catalogo. El vendedor de cada juego es un
 * {@code username} que {@code UsuariosDataInitializer} ({@code @Order(1)})
 * ya sembro como perfil.
 *
 * El sembrado es idempotente por nombre. Cada juego nuevo arranca con stock
 * 2-3; despues lo gestionan admin/vendedor desde la app y NO se toca en los
 * reinicios.
 */
@Component
@Order(2)
public class CatalogoDataInitializer implements CommandLineRunner {

    private final SeccionRepository seccionRepository;
    private final JuegoRepository juegoRepository;
    private final CompraRepository compraRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private static final Color[][] PALETA = {
            {new Color(29, 32, 92), new Color(111, 66, 193)},
            {new Color(92, 30, 20), new Color(191, 130, 30)},
            {new Color(15, 84, 74), new Color(46, 204, 113)},
            {new Color(15, 76, 90), new Color(52, 152, 219)},
            {new Color(58, 58, 58), new Color(230, 126, 34)},
            {new Color(74, 21, 75), new Color(214, 48, 141)},
            {new Color(12, 58, 74), new Color(0, 173, 181)},
            {new Color(66, 32, 6), new Color(224, 122, 63)},
    };

    public CatalogoDataInitializer(SeccionRepository seccionRepository, JuegoRepository juegoRepository,
                                   CompraRepository compraRepository) {
        this.seccionRepository = seccionRepository;
        this.juegoRepository = juegoRepository;
        this.compraRepository = compraRepository;
    }

    @Override
    public void run(String... args) {
        repararImagenesFaltantes();

        boolean baseVacia = juegoRepository.count() == 0 && seccionRepository.count() == 0;
        if (baseVacia) {
            limpiarImagenesViejas();
        }

        Seccion accion = seccion("Accion", "Juegos rapidos, disparos y combate");
        Seccion rpg = seccion("RPG", "Juegos de rol con historia y progresion");
        Seccion deportes = seccion("Deportes", "Simuladores deportivos");
        Seccion estrategia = seccion("Estrategia", "Juegos de gestion y tactica");
        Seccion aventura = seccion("Aventura", "Exploracion, plataformas y narrativa");

        Path dir = Paths.get(uploadDir);

        Juego galaxyRaiders = juego("Galaxy Raiders", "Shooter espacial cooperativo", "39.99", 3, accion, "vendedor1", dir);
        Juego reinosPerdidos = juego("Reinos Perdidos", "RPG de mundo abierto con crafting", "59.99", 3, rpg, "vendedor1", dir);
        Juego pixelKnights = juego("Pixel Knights", "Aventura RPG retro en pixel art", "19.99", 2, rpg, "vendedor2", dir);
        juego("Turbo League", "Carreras arcade de futbol con autos", "29.99", 3, deportes, "vendedor2", dir);
        juego("Imperios de Acero", "Estrategia por turnos de construccion de imperios", "34.99", 2, estrategia, "vendedor1", dir);
        Juego neonDrift = juego("Neon Drift", "Carreras nocturnas con estetica synthwave", "24.99", 3, deportes, "vendedor2", dir);
        juego("Cripta del Rey Lich", "RPG de mazmorras por turnos", "44.99", 2, rpg, "vendedor1", dir);
        juego("Comandos de Hierro", "Estrategia en tiempo real ambientada en los 40s", "27.99", 3, estrategia, "vendedor2", dir);
        juego("Salto Estelar", "Plataformas de precision en gravedad variable", "14.99", 2, accion, "vendedor1", dir);
        juego("Bosque de las Sombras", "Aventura de exploracion y sigilo", "32.99", 3, aventura, "vendedor2", dir);
        juego("Copa Mundial Retro", "Futbol arcade con equipos historicos", "19.99", 2, deportes, "vendedor1", dir);
        juego("Mazmorras Infinitas", "Roguelike con generacion procedural", "22.99", 3, rpg, "vendedor2", dir);
        juego("Titanes del Espacio", "Shooter de jefes gigantes", "49.99", 2, accion, "vendedor2", dir);

        if (baseVacia) {
            comprarSemilla(galaxyRaiders, "comprador1");
            comprarSemilla(pixelKnights, "comprador1");
            comprarSemilla(pixelKnights, "comprador2");
            comprarSemilla(reinosPerdidos, "comprador2");
            comprarSemilla(neonDrift, "comprador3");
        }

        System.out.println("[negocio-service] catalogo verificado (5 secciones, 13 juegos)");
    }

    private Seccion seccion(String nombre, String descripcion) {
        return seccionRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> seccionRepository.save(new Seccion(nombre, descripcion)));
    }

    /** Crea el juego si no existe (por nombre). Solo genera la portada al crearlo. */
    private Juego juego(String nombre, String descripcion, String precio, int stock, Seccion seccion,
                        String vendedorUsername, Path dir) {
        return juegoRepository.findByNombreIgnoreCase(nombre).orElseGet(() -> {
            Color[] colores = PALETA[Math.floorMod(nombre.hashCode(), PALETA.length)];
            String etiqueta = seccion != null ? seccion.getNombre() : "Juego";
            Juego juego = new Juego();
            juego.setNombre(nombre);
            juego.setDescripcion(descripcion);
            juego.setPrecio(new BigDecimal(precio));
            juego.setStock(stock);
            juego.setSeccion(seccion);
            juego.setVendedorUsername(vendedorUsername);
            juego.setImagenUrl(GeneradorPortadas.generar(dir, nombre, etiqueta, colores[0], colores[1]));
            return juegoRepository.save(juego);
        });
    }

    private void comprarSemilla(Juego juego, String compradorUsername) {
        if (compraRepository.existsByJuegoIdAndCompradorUsername(juego.getId(), compradorUsername)) {
            return;
        }
        Compra compra = new Compra();
        compra.setJuegoId(juego.getId());
        compra.setNombreJuego(juego.getNombre());
        compra.setImagenUrl(juego.getImagenUrl());
        compra.setPrecio(juego.getPrecio());
        compra.setCompradorUsername(compradorUsername);
        compraRepository.save(compra);
        juego.setStock(Math.max(0, juego.getStock() - 1));
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
            System.out.println("[negocio-service] se regeneraron " + reparadas + " portada(s) que faltaban en disco.");
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
