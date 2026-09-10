package com.gamestore.auth.config;

import com.gamestore.auth.model.Credential;
import com.gamestore.auth.repository.CredentialRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Siembra las credenciales de los usuarios de prueba. usuarios-service
 * siembra los perfiles correspondientes por su lado; ambos coinciden por
 * {@code username} (misma lista, misma clave natural).
 *
 *   admin       / admin123
 *   vendedor1   / vendedor123
 *   vendedor2   / vendedor123
 *   comprador1..comprador5 / comprador123
 *
 * Es idempotente por username: agregar usuarios a esta lista los crea al
 * proximo arranque sin tocar los que ya existen.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final CredentialRepository repo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CredentialRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearSiFalta("admin", "admin123", "admin@tiendajuegos.com");
        crearSiFalta("vendedor1", "vendedor123", "vendedor1@tiendajuegos.com");
        crearSiFalta("vendedor2", "vendedor123", "vendedor2@tiendajuegos.com");
        for (int i = 1; i <= 5; i++) {
            crearSiFalta("comprador" + i, "comprador123", "comprador" + i + "@tiendajuegos.com");
        }
        System.out.println("[auth-service] credenciales de prueba verificadas (admin, 2 vendedores, 5 compradores)");
    }

    private void crearSiFalta(String username, String rawPassword, String email) {
        if (repo.existsByUsername(username)) {
            return;
        }
        repo.save(new Credential(username, passwordEncoder.encode(rawPassword), email));
    }
}
