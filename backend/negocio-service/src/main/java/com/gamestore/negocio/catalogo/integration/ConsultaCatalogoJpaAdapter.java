package com.gamestore.negocio.catalogo.integration;

import com.gamestore.negocio.catalogo.repository.JuegoRepository;
import com.gamestore.negocio.catalogo.repository.SeccionRepository;
import com.gamestore.negocio.usuario.spi.ConsultaCatalogo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa el puerto {@link ConsultaCatalogo} del sub-dominio usuarios
 * usando los repositorios del catalogo. Reemplaza en memoria al viejo
 * {@code usuarios.client.CatalogoClient} (HTTP + circuit breaker).
 */
@Component
public class ConsultaCatalogoJpaAdapter implements ConsultaCatalogo {

    private final JuegoRepository juegoRepository;
    private final SeccionRepository seccionRepository;

    public ConsultaCatalogoJpaAdapter(JuegoRepository juegoRepository, SeccionRepository seccionRepository) {
        this.juegoRepository = juegoRepository;
        this.seccionRepository = seccionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Totales totales() {
        return new Totales(juegoRepository.count(), seccionRepository.count());
    }

    @Override
    @Transactional(readOnly = true)
    public long juegosDeVendedor(String username) {
        return juegoRepository.countByVendedorUsername(username);
    }
}
