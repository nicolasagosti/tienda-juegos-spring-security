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
 *   admin      / admin123
 *   vendedor1  / vendedor123
 *   vendedor2  / vendedor123
 *   comprador1 / comprador123
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
        if (repo.count() > 0) {
            return;
        }
        crear("admin", "admin123", "admin@tiendajuegos.com");
        crear("vendedor1", "vendedor123", "vendedor1@tiendajuegos.com");
        crear("vendedor2", "vendedor123", "vendedor2@tiendajuegos.com");
        crear("comprador1", "comprador123", "comprador1@tiendajuegos.com");
        System.out.println("[auth-service] credenciales de prueba creadas (admin/admin123, etc.)");
    }

    private void crear(String username, String rawPassword, String email) {
        repo.save(new Credential(username, passwordEncoder.encode(rawPassword), email));
    }
}
