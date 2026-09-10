package com.gamestore.negocio.usuarios.integration;

import com.gamestore.negocio.catalogo.spi.ResolucionVendedores;
import com.gamestore.negocio.usuarios.model.Usuario;
import com.gamestore.negocio.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementa el puerto {@link ResolucionVendedores} del sub-dominio catalogo
 * usando el repositorio de usuarios. Es el reemplazo en memoria del viejo
 * {@code catalogo.client.UsuariosClient} (que iba por HTTP a usuarios-service).
 *
 * Vive en el paquete {@code usuarios.integration} porque es usuarios el que
 * "publica" esta capacidad; catalogo solo depende de la interfaz.
 */
@Component
public class ResolucionVendedoresJpaAdapter implements ResolucionVendedores {

    private final UsuarioRepository usuarioRepository;

    public ResolucionVendedoresJpaAdapter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Vendedor> porUsernames(Collection<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return Map.of();
        }
        List<String> lista = usernames.stream().distinct().toList();
        return usuarioRepository.findByUsernameIn(lista).stream()
                .collect(Collectors.toMap(Usuario::getUsername, ResolucionVendedoresJpaAdapter::aVendedor, (a, b) -> a));
    }

    private static Vendedor aVendedor(Usuario u) {
        return new Vendedor(
                u.getId(),
                u.getUsername(),
                u.getNombreCompleto(),
                u.getEmail(),
                u.getRol() == null ? null : u.getRol().name(),
                u.isHabilitado());
    }
}
