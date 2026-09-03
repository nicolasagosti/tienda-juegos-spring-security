package com.gamestore.usuarios.web;

import com.gamestore.usuarios.service.UsuarioService;
import com.gamestore.usuarios.web.Dtos.UsuarioDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API interna: la consumen auth-service (login, refresh, login con Google)
 * y catalogo-service (resolver el vendedor de cada juego). Detras de
 * InternalTokenFilter; nunca se expone por el gateway.
 */
@RestController
@RequestMapping("/internal/usuarios")
public class InternalUsuariosController {

    private final UsuarioService usuarioService;

    public InternalUsuariosController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/by-username/{username}")
    public UsuarioDTO porUsername(@PathVariable String username) {
        return UsuarioDTO.from(usuarioService.buscarPorUsername(username));
    }

    @GetMapping("/{id}")
    public UsuarioDTO porId(@PathVariable Long id) {
        return UsuarioDTO.from(usuarioService.buscarPorId(id));
    }

    /** Resolucion en lote por id: {@code GET /internal/usuarios?ids=1,2,3}. */
    @GetMapping(params = "ids")
    public List<UsuarioDTO> porIds(@RequestParam List<Long> ids) {
        return usuarioService.buscarPorIds(ids).stream().map(UsuarioDTO::from).toList();
    }

    /** Resolucion en lote por username: {@code GET /internal/usuarios?usernames=a,b}. La usa el catalogo. */
    @GetMapping(params = "usernames")
    public List<UsuarioDTO> porUsernames(@RequestParam List<String> usernames) {
        return usuarioService.buscarPorUsernames(usernames).stream().map(UsuarioDTO::from).toList();
    }

    @PostMapping("/google")
    public UsuarioDTO buscarOCrearGoogle(@RequestBody Map<String, String> body) {
        String nombre = body.get("nombre");
        return UsuarioDTO.from(usuarioService.buscarOCrearDesdeGoogle(body.get("email"),
                nombre == null || nombre.isBlank() ? null : nombre));
    }
}
