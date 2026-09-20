package com.gamestore.negocio.usuario.controller;

import com.gamestore.common.security.AuthPrincipal;
import com.gamestore.negocio.usuario.service.UsuarioService;
import com.gamestore.negocio.usuario.dto.ActualizarUsuarioRequestDto;
import com.gamestore.negocio.usuario.dto.CrearUsuarioRequestDto;
import com.gamestore.negocio.usuario.dto.UsuarioDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public List<UsuarioDto> listar() {
        return usuarioService.listarTodos().stream().map(UsuarioDto::from).toList();
    }

    @PostMapping
    public UsuarioDto crear(@Valid @RequestBody CrearUsuarioRequestDto req) {
        return UsuarioDto.from(usuarioService.crearUsuario(
                req.username(), req.password(), req.nombreCompleto(), req.email(), req.rol()));
    }

    @PutMapping("/{id}")
    public UsuarioDto actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequestDto req) {
        return UsuarioDto.from(usuarioService.actualizarUsuario(
                id, req.nombreCompleto(), req.email(), req.rol(), req.habilitado(), req.nuevaPassword()));
    }

    @PostMapping("/{id}/toggle")
    public UsuarioDto toggle(@PathVariable Long id) {
        usuarioService.alternarHabilitado(id);
        return UsuarioDto.from(usuarioService.buscarPorId(id));
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
