package com.example.tiendajuegos.api.controller;

import com.example.tiendajuegos.api.dto.JuegoDTO;
import com.example.tiendajuegos.model.Juego;
import com.example.tiendajuegos.model.Rol;
import com.example.tiendajuegos.model.Seccion;
import com.example.tiendajuegos.model.Usuario;
import com.example.tiendajuegos.repository.SeccionRepository;
import com.example.tiendajuegos.security.CustomUserDetails;
import com.example.tiendajuegos.service.JuegoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * Version REST (JSON) del catalogo de juegos, consumida por el frontend
 * React. Las reglas de negocio son identicas a JuegoController (el que usa
 * las vistas Thymeleaf) porque llaman al mismo JuegoService: el rol se
 * revisa a nivel de URL (SecurityConfig) y de metodo (@PreAuthorize), y la
 * pertenencia ("es tu juego?") se revisa a mano contra el dato.
 */
@RestController
@RequestMapping("/api/juegos")
public class JuegoApiController {

    private final JuegoService juegoService;
    private final SeccionRepository seccionRepository;

    public JuegoApiController(JuegoService juegoService, SeccionRepository seccionRepository) {
        this.juegoService = juegoService;
        this.seccionRepository = seccionRepository;
    }

    @GetMapping
    public List<JuegoDTO> listar(@AuthenticationPrincipal CustomUserDetails principal) {
        return juegoService.listarTodos().stream()
                .map(j -> JuegoDTO.from(j, principal.getUsuario()))
                .toList();
    }

    @GetMapping("/{id}")
    public JuegoDTO obtener(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        return JuegoDTO.from(juegoService.buscarPorId(id), principal.getUsuario());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public JuegoDTO crear(@RequestParam String nombre,
                           @RequestParam(required = false) String descripcion,
                           @RequestParam BigDecimal precio,
                           @RequestParam(required = false) Long seccionId,
                           @RequestParam(required = false) MultipartFile imagen,
                           @AuthenticationPrincipal CustomUserDetails principal) {
        Seccion seccion = (seccionId != null) ? seccionRepository.findById(seccionId).orElse(null) : null;
        Juego juego = juegoService.crear(nombre, descripcion, precio, seccion, principal.getUsuario(), imagen);
        return JuegoDTO.from(juego, principal.getUsuario());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public JuegoDTO actualizar(@PathVariable Long id,
                                @RequestParam String nombre,
                                @RequestParam(required = false) String descripcion,
                                @RequestParam BigDecimal precio,
                                @RequestParam(required = false) Long seccionId,
                                @RequestParam(required = false) MultipartFile imagen,
                                @AuthenticationPrincipal CustomUserDetails principal) {
        Juego existente = juegoService.buscarPorId(id);
        verificarPropietarioOAdmin(existente, principal.getUsuario());
        Seccion seccion = (seccionId != null) ? seccionRepository.findById(seccionId).orElse(null) : null;
        Juego actualizado = juegoService.actualizar(id, nombre, descripcion, precio, seccion, imagen);
        return JuegoDTO.from(actualizado, principal.getUsuario());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        Juego juego = juegoService.buscarPorId(id);
        verificarPropietarioOAdmin(juego, principal.getUsuario());
        juegoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private void verificarPropietarioOAdmin(Juego juego, Usuario usuarioActual) {
        if (usuarioActual.getRol() != Rol.ADMIN
                && !juego.getVendedor().getId().equals(usuarioActual.getId())) {
            throw new AccessDeniedException("No podes modificar juegos de otro vendedor");
        }
    }
}
