package com.gamestore.auth.service;

import com.gamestore.auth.model.RefreshToken;
import com.gamestore.auth.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Emision, rotacion y revocacion de refresh tokens. Sin cambios de logica
 * respecto del monolito: cada uso ROTA el token (el viejo se revoca), y
 * reusar uno revocado no sirve -> es la señal de que se filtro.
 */
@Service
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration-days:7}")
    private long refreshExpirationDias;

    private final RefreshTokenRepository repo;

    public RefreshTokenService(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    public RefreshToken crear(String username) {
        return repo.save(new RefreshToken(
                UUID.randomUUID().toString(),
                username,
                LocalDateTime.now().plusDays(refreshExpirationDias)));
    }

    public Optional<RefreshToken> buscarValido(String token) {
        return repo.findByToken(token).filter(RefreshToken::esValido);
    }

    @Transactional
    public RefreshToken rotar(RefreshToken actual) {
        actual.setRevocado(true);
        repo.save(actual);
        return crear(actual.getUsername());
    }

    @Transactional
    public void revocar(RefreshToken token) {
        token.setRevocado(true);
        repo.save(token);
    }
}
