package com.gamestore.negocio.usuario.service;

import com.gamestore.common.web.exception.ConflictException;
import com.gamestore.negocio.usuario.client.AuthClient;
import com.gamestore.negocio.usuario.model.Rol;
import com.gamestore.negocio.usuario.model.Usuario;
import com.gamestore.negocio.usuario.repository.UsuarioRepository;
import com.gamestore.negocio.usuario.spi.ConsultaCatalogo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ABM de usuarios del ADMIN. El perfil vive aca; la credencial (hash) vive
 * en auth-service, asi que el alta / cambio de password / baja siguen siendo
 * de DOS pasos entre dos procesos.
 *
 * En esta iteracion NO hay saga ni outbox: si el segundo paso falla,
 * compensamos a mano en un catch. La consulta al catalogo, en cambio, ahora
 * es una llamada en memoria ({@link ConsultaCatalogo}).
 */
@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository repo;
    private final AuthClient authClient;
    private final ConsultaCatalogo catalogo;

    public UsuarioService(UsuarioRepository repo, AuthClient authClient, ConsultaCatalogo catalogo) {
        this.repo = repo;
        this.authClient = authClient;
        this.catalogo = catalogo;
    }

    public List<Usuario> listarTodos() {
        return repo.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
    }

    public Usuario buscarPorUsername(String username) {
        return repo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
    }

    public long contarPorRol(Rol rol) {
        return repo.countByRol(rol);
    }

    public List<Usuario> buscarPorIds(List<Long> ids) {
        return repo.findByIdIn(ids);
    }

    public List<Usuario> buscarPorUsernames(List<String> usernames) {
        return repo.findByUsernameIn(usernames);
    }

    /**
     * Alta: 1) valida + guarda el perfil local, 2) pide a auth-service que
     * cree la credencial. Si (2) falla, se borra el perfil (compensacion).
     */
    @Transactional
    public Usuario crearUsuario(String username, String rawPassword, String nombreCompleto, String email, Rol rol) {
        if (repo.existsByUsername(username)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre de usuario");
        }
        if (email != null && !email.isBlank() && repo.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        Usuario guardado = repo.saveAndFlush(new Usuario(username, nombreCompleto, email, rol));
        try {
            authClient.crearCredencial(username, email, rawPassword);
        } catch (RuntimeException e) {
            log.warn("Fallo crear la credencial de '{}' en auth-service; revierto el perfil", username, e);
            repo.delete(guardado);
            throw new IllegalStateException("No se pudo crear el usuario (auth-service): " + e.getMessage(), e);
        }
        return guardado;
    }

    /** Edicion de perfil; la contrasena solo se toca si vino una nueva -&gt; se delega a auth-service. */
    @Transactional
    public Usuario actualizarUsuario(Long id, String nombreCompleto, String email, Rol rol,
                                     boolean habilitado, String nuevaPasswordOpcional) {
        Usuario usuario = buscarPorId(id);
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setEmail(email);
        usuario.setRol(rol);
        usuario.setHabilitado(habilitado);
        repo.save(usuario);

        if (nuevaPasswordOpcional != null && !nuevaPasswordOpcional.isBlank()) {
            authClient.cambiarPassword(usuario.getUsername(), nuevaPasswordOpcional);
        }
        return usuario;
    }

    @Transactional
    public void alternarHabilitado(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setHabilitado(!usuario.isHabilitado());
        repo.save(usuario);
    }

    /**
     * Baja: primero pregunta al catalogo (en memoria) si el usuario tiene
     * juegos publicados, despues borra el perfil y pide a auth-service borrar
     * la credencial.
     */
    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = buscarPorId(id);
        if (catalogo.juegosDeVendedor(usuario.getUsername()) > 0) {
            throw new ConflictException(
                    "No se puede eliminar: el usuario tiene juegos publicados. Eliminalos primero.");
        }
        repo.delete(usuario);
        authClient.eliminarCredencial(usuario.getUsername());
    }

    /**
     * Lo llama auth-service tras un login con Google exitoso. Si ya hay un
     * usuario con ese email lo reutiliza; si no, lo crea con rol COMPRADOR
     * (el mas restrictivo: nadie se autopromueve entrando con Google).
     */
    @Transactional
    public Usuario buscarOCrearDesdeGoogle(String email, String nombre) {
        return repo.findByEmail(email).orElseGet(() -> {
            String base = email.substring(0, email.indexOf('@'));
            String username = generarUsernameDisponible(base);
            return repo.save(new Usuario(username,
                    nombre != null && !nombre.isBlank() ? nombre : username, email, Rol.COMPRADOR));
        });
    }

    private String generarUsernameDisponible(String base) {
        String candidato = base;
        int sufijo = 1;
        while (repo.existsByUsername(candidato)) {
            candidato = base + sufijo++;
        }
        return candidato;
    }
}
