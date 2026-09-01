package com.example.tiendajuegos.config;

import com.example.tiendajuegos.model.Juego;
import com.example.tiendajuegos.model.Rol;
import com.example.tiendajuegos.model.Seccion;
import com.example.tiendajuegos.model.Usuario;
import com.example.tiendajuegos.repository.JuegoRepository;
import com.example.tiendajuegos.repository.SeccionRepository;
import com.example.tiendajuegos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * Carga datos de ejemplo al arrancar la app (perfecto para una demo con
 * base de datos en memoria H2, que se reinicia vacia cada vez).
 *
 * Usuarios creados:
 *   admin      / admin123      -> ADMIN
 *   vendedor1  / vendedor123   -> VENDEDOR
 *   vendedor2  / vendedor123   -> VENDEDOR
 *   comprador1 / comprador123  -> COMPRADOR
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final SeccionRepository seccionRepository;
    private final JuegoRepository juegoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public DataInitializer(UsuarioRepository usuarioRepository,
                            SeccionRepository seccionRepository,
                            JuegoRepository juegoRepository,
                            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.seccionRepository = seccionRepository;
        this.juegoRepository = juegoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return; // ya hay datos, no duplicar
        }

        limpiarImagenesViejas();

        Usuario admin = crearUsuario("admin", "admin123", "Administrador General", "admin@tiendajuegos.com", Rol.ADMIN);
        Usuario vendedor1 = crearUsuario("vendedor1", "vendedor123", "Nintenrog Games", "vendedor1@tiendajuegos.com", Rol.VENDEDOR);
        Usuario vendedor2 = crearUsuario("vendedor2", "vendedor123", "Pixel Studios", "vendedor2@tiendajuegos.com", Rol.VENDEDOR);
        crearUsuario("comprador1", "comprador123", "Juan Comprador", "comprador1@tiendajuegos.com", Rol.COMPRADOR);

        Seccion accion = seccionRepository.save(new Seccion("Accion", "Juegos rapidos, disparos y combate"));
        Seccion rpg = seccionRepository.save(new Seccion("RPG", "Juegos de rol con historia y progresion"));
        Seccion deportes = seccionRepository.save(new Seccion("Deportes", "Simuladores deportivos"));
        Seccion estrategia = seccionRepository.save(new Seccion("Estrategia", "Juegos de gestion y tactica"));

        Path dir = Paths.get(uploadDir);

        guardarJuego("Galaxy Raiders", "Shooter espacial cooperativo", "39.99", accion, vendedor1,
                GeneradorPortadas.generar(dir, "Galaxy Raiders", "Accion", new Color(29, 32, 92), new Color(111, 66, 193)));

        guardarJuego("Reinos Perdidos", "RPG de mundo abierto con crafting", "59.99", rpg, vendedor1,
                GeneradorPortadas.generar(dir, "Reinos Perdidos", "RPG", new Color(92, 30, 20), new Color(191, 130, 30)));

        guardarJuego("Pixel Knights", "Aventura RPG retro en pixel art", "19.99", rpg, vendedor2,
                GeneradorPortadas.generar(dir, "Pixel Knights", "RPG", new Color(15, 84, 74), new Color(46, 204, 113)));

        guardarJuego("Turbo League", "Carreras arcade de futbol con autos", "29.99", deportes, vendedor2,
                GeneradorPortadas.generar(dir, "Turbo League", "Deportes", new Color(15, 76, 90), new Color(52, 152, 219)));

        guardarJuego("Imperios de Acero", "Estrategia por turnos de construccion de imperios", "34.99", estrategia, vendedor1,
                GeneradorPortadas.generar(dir, "Imperios de Acero", "Estrategia", new Color(58, 58, 58), new Color(230, 126, 34)));

        System.out.println("=====================================================");
        System.out.println(" Datos de prueba cargados. Usuarios disponibles:");
        System.out.println("  admin      / admin123      (ADMIN)");
        System.out.println("  vendedor1  / vendedor123   (VENDEDOR)");
        System.out.println("  vendedor2  / vendedor123   (VENDEDOR)");
        System.out.println("  comprador1 / comprador123  (COMPRADOR)");
        System.out.println("=====================================================");
    }

    private Usuario crearUsuario(String username, String rawPassword, String nombre, String email, Rol rol) {
        Usuario usuario = new Usuario(username, passwordEncoder.encode(rawPassword), nombre, email, rol);
        return usuarioRepository.save(usuario);
    }

    private void guardarJuego(String nombre, String descripcion, String precio, Seccion seccion, Usuario vendedor, String imagenUrl) {
        Juego juego = new Juego();
        juego.setNombre(nombre);
        juego.setDescripcion(descripcion);
        juego.setPrecio(new BigDecimal(precio));
        juego.setSeccion(seccion);
        juego.setVendedor(vendedor);
        juego.setImagenUrl(imagenUrl);
        juegoRepository.save(juego);
    }

    /**
     * La base H2 es en memoria (se reinicia vacia en cada arranque) pero
     * /uploads vive en disco y persiste entre reinicios. Sin este borrado,
     * cada restart de la app dejaria portadas generadas huerfanas
     * acumulandose para siempre. Solo se ejecuta cuando vamos a recrear
     * los datos de cero (ver el "return" de arriba).
     */
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
                            // best-effort: si algun archivo esta bloqueado, no frenamos el arranque
                        }
                    });
        } catch (IOException ignored) {
            // idem
        }
    }
}
