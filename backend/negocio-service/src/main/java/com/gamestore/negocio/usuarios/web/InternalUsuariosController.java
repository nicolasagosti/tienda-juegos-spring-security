package com.gamestore.negocio.usuarios.web;

import com.gamestore.negocio.usuarios.service.UsuarioService;
import com.gamestore.negocio.usuarios.web.dto.UsuarioDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * API interna: la sigue consumiendo <b>auth-service</b> (login, refresh,
 * login con Google). Detras de InternalTokenFilter; nunca se expone por el
 * gateway.
 *
 * Antes tambien la usaba catalogo-service para resolver el vendedor de cada
 * juego; eso ahora es una llamada en memoria ({@code catalogo.spi.ResolucionVendedores}).
 */
@RestController
@RequestMapping("/internal/usuarios")
public class InternalUsuariosController {

    private final UsuarioService usuarioService;

    public InternalUsuariosController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/by-username/{username}")
    public UsuarioDto porUsername(@PathVariable String username) {
        return UsuarioDto.from(usuarioService.buscarPorUsername(username));
    }

    @GetMapping("/{id}")
    public UsuarioDto porId(@PathVariable Long id) {
        return UsuarioDto.from(usuarioService.buscarPorId(id));
    }

    /** Resolucion en lote por id: {@code GET /internal/usuarios?ids=1,2,3}. */
    @GetMapping(params = "ids")
    public List<UsuarioDto> porIds(@RequestParam List<Long> ids) {
        return usuarioService.buscarPorIds(ids).stream().map(UsuarioDto::from).toList();
    }

    /** Resolucion en lote por username: {@code GET /internal/usuarios?usernames=a,b}. */
    @GetMapping(params = "usernames")
    public List<UsuarioDto> porUsernames(@RequestParam List<String> usernames) {
        return usuarioService.buscarPorUsernames(usernames).stream().map(UsuarioDto::from).toList();
    }

    @PostMapping("/google")
    public UsuarioDto buscarOCrearGoogle(@RequestBody Map<String, String> body) {
        String nombre = body.get("nombre");
        return UsuarioDto.from(usuarioService.buscarOCrearDesdeGoogle(body.get("email"),
                nombre == null || nombre.isBlank() ? null : nombre));
    }
}
