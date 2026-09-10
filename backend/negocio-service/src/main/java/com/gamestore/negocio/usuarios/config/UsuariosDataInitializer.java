package com.gamestore.negocio.usuarios.config;

import com.gamestore.negocio.usuarios.model.Rol;
import com.gamestore.negocio.usuarios.model.Usuario;
import com.gamestore.negocio.usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Siembra los PERFILES de los usuarios de prueba. auth-service siembra las
 * credenciales por su lado; coinciden por {@code username}.
 *
 * Corre ANTES que {@code CatalogoDataInitializer} ({@code @Order(1)}) para
 * que, si algun dia se agrega un chequeo de existencia de vendedor, los
 * perfiles ya esten.
 */
@Component
@Order(1)
public class UsuariosDataInitializer implements CommandLineRunner {

    private static final String[] NOMBRES_COMPRADORES = {
            "Juan Comprador", "Malena Rios", "Diego Fuentes", "Sofia Paredes", "Tomas Aguirre"
    };

    private final UsuarioRepository repo;

    public UsuariosDataInitializer(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        crearSiFalta("admin", "Administrador General", "admin@tiendajuegos.com", Rol.ADMIN);
        crearSiFalta("vendedor1", "Nintenrog Games", "vendedor1@tiendajuegos.com", Rol.VENDEDOR);
        crearSiFalta("vendedor2", "Pixel Studios", "vendedor2@tiendajuegos.com", Rol.VENDEDOR);
        for (int i = 1; i <= 5; i++) {
            crearSiFalta("comprador" + i, NOMBRES_COMPRADORES[i - 1],
                    "comprador" + i + "@tiendajuegos.com", Rol.COMPRADOR);
        }
        System.out.println("[negocio-service] perfiles de prueba verificados (admin, 2 vendedores, 5 compradores)");
    }

    private void crearSiFalta(String username, String nombreCompleto, String email, Rol rol) {
        if (repo.existsByUsername(username)) {
            return;
        }
        repo.save(new Usuario(username, nombreCompleto, email, rol));
    }
}
