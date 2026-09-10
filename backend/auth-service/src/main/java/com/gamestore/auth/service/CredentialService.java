package com.gamestore.auth.service;

import com.gamestore.auth.model.Credential;
import com.gamestore.auth.repository.CredentialRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * ABM de credenciales. Lo dispara usuarios-service por la API interna
 * cuando el ADMIN da de alta / edita / borra un usuario: el perfil se crea
 * alla, la credencial aca.
 */
@Service
public class CredentialService {

    private final CredentialRepository repo;
    private final PasswordEncoder passwordEncoder;

    public CredentialService(CredentialRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void crear(String username, String email, String rawPassword) {
        if (repo.existsByUsername(username)) {
            throw new IllegalArgumentException("Ya existe una credencial para: " + username);
        }
        repo.save(new Credential(username, passwordEncoder.encode(rawPassword), email));
    }

    @Transactional
    public void cambiarPassword(String username, String rawPassword) {
        Credential cred = repo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Credencial no encontrada: " + username));
        cred.setPasswordHash(passwordEncoder.encode(rawPassword));
        repo.save(cred);
    }

    @Transactional
    public void eliminar(String username) {
        repo.findByUsername(username).ifPresent(repo::delete);
    }

    /**
     * El login con Google crea el perfil en usuarios-service; aca nos
     * aseguramos de que exista tambien una credencial (con password random
     * inutilizable) para que el usuario pueda despues activar 2FA o que el
     * refresh token tenga a quien apuntar.
     */
    @Transactional
    public void asegurarParaGoogle(String username, String email) {
        if (!repo.existsByUsername(username)) {
            repo.save(new Credential(username,
                    passwordEncoder.encode(UUID.randomUUID().toString()), email));
        }
    }
}
