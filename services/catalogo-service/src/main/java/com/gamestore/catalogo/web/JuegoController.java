package com.gamestore.catalogo.web;

import com.gamestore.catalogo.model.Juego;
import com.gamestore.catalogo.model.Seccion;
import com.gamestore.catalogo.repository.SeccionRepository;
import com.gamestore.catalogo.service.JuegoService;
import com.gamestore.catalogo.web.Dtos.JuegoDTO;
import com.gamestore.common.security.AuthPrincipal;
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
 * Version REST del catalogo, misma forma que el JuegoApiController del
 * monolito. La diferencia: el "vendedor" y el "puedeEditar" se resuelven
 * con datos del JWT + una llamada a usuarios-service (ver JuegoDtoMapper).
 */
@RestController
@RequestMapping("/api/juegos")
public class JuegoController {

    private final JuegoService juegoService;
    private final SeccionRepository seccionRepository;
    private final JuegoDtoMapper mapper;

    public JuegoController(JuegoService juegoService, SeccionRepository seccionRepository, JuegoDtoMapper mapper) {
        this.juegoService = juegoService;
        this.seccionRepository = seccionRepository;
        this.mapper = mapper;
    }

    @GetMapping
    public List<JuegoDTO> listar(@AuthenticationPrincipal AuthPrincipal principal) {
        return mapper.aDtos(juegoService.listarTodos(), principal);
    }

    @GetMapping("/{id}")
    public JuegoDTO obtener(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return mapper.aDto(juegoService.buscarPorId(id), principal);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public JuegoDTO crear(@RequestParam String nombre,
                          @RequestParam(required = false) String descripcion,
                          @RequestParam BigDecimal precio,
                          @RequestParam(required = false) Long seccionId,
                          @RequestParam(required = false) MultipartFile imagen,
                          @AuthenticationPrincipal AuthPrincipal principal) {
        Seccion seccion = (seccionId != null) ? seccionRepository.findById(seccionId).orElse(null) : null;
        Juego juego = juegoService.crear(nombre, descripcion, precio, seccion, principal.username(), imagen);
        return mapper.aDto(juego, principal);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public JuegoDTO actualizar(@PathVariable Long id,
                               @RequestParam String nombre,
                               @RequestParam(required = false) String descripcion,
                               @RequestParam BigDecimal precio,
                               @RequestParam(required = false) Long seccionId,
                               @RequestParam(required = false) MultipartFile imagen,
                               @AuthenticationPrincipal AuthPrincipal principal) {
        Juego existente = juegoService.buscarPorId(id);
        verificarPropietarioOAdmin(existente, principal);
        Seccion seccion = (seccionId != null) ? seccionRepository.findById(seccionId).orElse(null) : null;
        Juego actualizado = juegoService.actualizar(id, nombre, descripcion, precio, seccion, imagen);
        return mapper.aDto(actualizado, principal);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        Juego juego = juegoService.buscarPorId(id);
        verificarPropietarioOAdmin(juego, principal);
        juegoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private void verificarPropietarioOAdmin(Juego juego, AuthPrincipal principal) {
        if (!juegoService.puedeGestionar(juego, principal)) {
            throw new AccessDeniedException("No podes modificar juegos de otro vendedor");
        }
    }
}
