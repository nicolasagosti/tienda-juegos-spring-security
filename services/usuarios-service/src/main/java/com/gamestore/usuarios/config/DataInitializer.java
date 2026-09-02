package com.gamestore.usuarios.config;

import com.gamestore.usuarios.model.Rol;
import com.gamestore.usuarios.model.Usuario;
import com.gamestore.usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Siembra los PERFILES de los usuarios de prueba. auth-service siembra las
 * credenciales por su lado; coinciden por {@code username}.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository repo;

    public DataInitializer(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) {
            return;
        }
        repo.save(new Usuario("admin", "Administrador General", "admin@tiendajuegos.com", Rol.ADMIN));
        repo.save(new Usuario("vendedor1", "Nintenrog Games", "vendedor1@tiendajuegos.com", Rol.VENDEDOR));
        repo.save(new Usuario("vendedor2", "Pixel Studios", "vendedor2@tiendajuegos.com", Rol.VENDEDOR));
        repo.save(new Usuario("comprador1", "Juan Comprador", "comprador1@tiendajuegos.com", Rol.COMPRADOR));
        System.out.println("[usuarios-service] perfiles de prueba creados (admin, vendedor1, vendedor2, comprador1)");
    }
}
