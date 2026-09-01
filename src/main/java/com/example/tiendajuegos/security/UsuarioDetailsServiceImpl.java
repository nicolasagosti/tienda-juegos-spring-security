package com.example.tiendajuegos.security;

import com.example.tiendajuegos.model.Usuario;
import com.example.tiendajuegos.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Punto donde Spring Security va a buscar el usuario durante el login.
 * DaoAuthenticationProvider llama a loadUserByUsername(...) y despues
 * compara la contraseña ingresada (hasheandola) contra el hash guardado.
 */
@Service
public class UsuarioDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        return new CustomUserDetails(usuario);
    }
}
