package com.example.tiendajuegos.api.controller;

import com.example.tiendajuegos.api.dto.Requests.ActualizarUsuarioRequest;
import com.example.tiendajuegos.api.dto.Requests.CrearUsuarioRequest;
import com.example.tiendajuegos.api.dto.UsuarioDTO;
import com.example.tiendajuegos.security.CustomUserDetails;
import com.example.tiendajuegos.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Exclusivo del ADMIN: alta de usuarios con asignacion de categoria, edicion de perfiles, habilitar/deshabilitar y eliminar. */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioApiController {

    private final UsuarioService usuarioService;

    public UsuarioApiController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioDTO> listar() {
        return usuarioService.listarTodos().stream().map(UsuarioDTO::from).toList();
    }

    @PostMapping
    public UsuarioDTO crear(@RequestBody CrearUsuarioRequest req) {
        return UsuarioDTO.from(usuarioService.crearUsuario(req.username(), req.password(), req.nombreCompleto(), req.email(), req.rol()));
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
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal.getUsuario().getId().equals(id)) {
            throw new IllegalArgumentException("No podes eliminar tu propio usuario");
        }
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
