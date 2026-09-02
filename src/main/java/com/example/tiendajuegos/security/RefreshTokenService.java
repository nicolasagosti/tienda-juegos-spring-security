package com.example.tiendajuegos.security;

import com.example.tiendajuegos.model.RefreshToken;
import com.example.tiendajuegos.model.Usuario;
import com.example.tiendajuegos.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration-days:7}")
    private long refreshExpirationDias;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken crear(Usuario usuario) {
        RefreshToken token = new RefreshToken(
                UUID.randomUUID().toString(),
                usuario,
                LocalDateTime.now().plusDays(refreshExpirationDias)
        );
        return refreshTokenRepository.save(token);
    }

    public Optional<RefreshToken> buscarValido(String token) {
        return refreshTokenRepository.findByToken(token).filter(RefreshToken::esValido);
    }

    /**
     * "Rotacion": cada vez que se usa un refresh token para pedir un
     * access token nuevo, ese refresh token se revoca y se emite uno
     * nuevo. Si alguien roba un refresh token viejo y lo reusa, el
     * legitimo ya lo habra rotado -- el reuso de un token revocado es
     * justamente la señal de que algo se filtro.
     */
    @Transactional
    public RefreshToken rotar(RefreshToken actual) {
        actual.setRevocado(true);
        refreshTokenRepository.save(actual);
        return crear(actual.getUsuario());
    }

    @Transactional
    public void revocar(RefreshToken token) {
        token.setRevocado(true);
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void revocarTodosDe(Usuario usuario) {
        refreshTokenRepository.revocarTodosDe(usuario);
    }
}
