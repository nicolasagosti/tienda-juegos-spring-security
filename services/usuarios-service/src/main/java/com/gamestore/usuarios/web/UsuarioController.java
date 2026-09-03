package com.gamestore.usuarios.web;

import com.gamestore.common.security.AuthPrincipal;
import com.gamestore.usuarios.service.UsuarioService;
import com.gamestore.usuarios.web.Dtos.ActualizarUsuarioRequest;
import com.gamestore.usuarios.web.Dtos.CrearUsuarioRequest;
import com.gamestore.usuarios.web.Dtos.UsuarioDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Exclusivo del ADMIN, mismos endpoints que el UsuarioApiController del monolito. */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioDTO> listar() {
        return usuarioService.listarTodos().stream().map(UsuarioDTO::from).toList();
    }

    @PostMapping
    public UsuarioDTO crear(@RequestBody CrearUsuarioRequest req) {
        return UsuarioDTO.from(usuarioService.crearUsuario(
                req.username(), req.password(), req.nombreCompleto(), req.email(), req.rol()));
    }

    @PutMapping("/{id}")
    public UsuarioDTO actualizar(@PathVariable Long id, @RequestBody ActualizarUsuarioRequest req) {
        return UsuarioDTO.from(usuarioService.actualizarUsuario(
                id, req.nombreCompleto(), req.email(), req.rol(), req.habilitado(), req.nuevaPassword()));
    }

    @PostMapping("/{id}/toggle")
    public UsuarioDTO toggle(@PathVariable Long id) {
        usuarioService.alternarHabilitado(id);
        return UsuarioDTO.from(usuarioService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        if (principal.id() == id) {
            throw new IllegalArgumentException("No podes eliminar tu propio usuario");
        }
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
