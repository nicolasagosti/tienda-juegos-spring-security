package com.example.tiendajuegos.service;

import com.example.tiendajuegos.model.Rol;
import com.example.tiendajuegos.model.Usuario;
import com.example.tiendajuegos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Funciones que solo el ADMIN dispara desde el panel /admin:
 * alta de usuarios (con asignacion de rol), edicion de perfiles,
 * habilitar/deshabilitar y eliminar cuentas.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
    }

    /** Alta de usuario por parte del ADMIN, asignandole directamente una categoria (rol). */
    @Transactional
    public Usuario crearUsuario(String username, String rawPassword, String nombreCompleto, String email, Rol rol) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre de usuario");
        }
        Usuario usuario = new Usuario(username, passwordEncoder.encode(rawPassword), nombreCompleto, email, rol);
        return usuarioRepository.save(usuario);
    }

    /** Edicion de perfil: nombre, email y rol. La contraseña solo se cambia si se envia una nueva. */
    @Transactional
    public Usuario actualizarUsuario(Long id, String nombreCompleto, String email, Rol rol,
                                      boolean habilitado, String nuevaPasswordOpcional) {
        Usuario usuario = buscarPorId(id);
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setEmail(email);
        usuario.setRol(rol);
        usuario.setHabilitado(habilitado);
        if (nuevaPasswordOpcional != null && !nuevaPasswordOpcional.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(nuevaPasswordOpcional));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void alternarHabilitado(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setHabilitado(!usuario.isHabilitado());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    public long contarPorRol(Rol rol) {
        return usuarioRepository.findByRol(rol).size();
    }
}
