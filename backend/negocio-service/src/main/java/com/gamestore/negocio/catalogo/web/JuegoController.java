package com.gamestore.negocio.catalogo.web;

import com.gamestore.common.security.AuthPrincipal;
import com.gamestore.negocio.catalogo.model.Juego;
import com.gamestore.negocio.catalogo.model.Seccion;
import com.gamestore.negocio.catalogo.repository.SeccionRepository;
import com.gamestore.negocio.catalogo.service.JuegoService;
import com.gamestore.negocio.catalogo.web.dto.JuegoDto;
import com.gamestore.negocio.catalogo.web.dto.JuegoFormRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Version REST del catalogo, misma forma que el JuegoApiController del
 * monolito. El "vendedor" y el "puedeEditar" se resuelven con datos del JWT +
 * el sub-dominio usuarios en memoria (ver {@link JuegoDtoMapper}).
 *
 * Los cuerpos de alta/edicion se enlazan con {@link JuegoFormRequest} y se
 * validan con {@code @Valid}; {@code @Validated} a nivel de clase habilita
 * las constraints sobre parametros sueltos ({@code cantidad}).
 */
@RestController
@RequestMapping("/api/juegos")
@Validated
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
    public List<JuegoDto> listar(@AuthenticationPrincipal AuthPrincipal principal) {
        return mapper.aDtos(juegoService.listarTodos(), principal);
    }

    @GetMapping("/{id}")
    public JuegoDto obtener(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return mapper.aDto(juegoService.buscarPorId(id), principal);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public JuegoDto crear(@Valid @ModelAttribute JuegoFormRequest form,
                          @RequestParam(required = false) MultipartFile imagen,
                          @AuthenticationPrincipal AuthPrincipal principal) {
        Seccion seccion = buscarSeccion(form.getSeccionId());
        Juego juego = juegoService.crear(form.getNombre(), form.getDescripcion(), form.getPrecio(),
                form.getStock(), seccion, principal.username(), imagen);
        return mapper.aDto(juego, principal);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public JuegoDto actualizar(@PathVariable Long id,
                               @Valid @ModelAttribute JuegoFormRequest form,
                               @RequestParam(required = false) MultipartFile imagen,
                               @AuthenticationPrincipal AuthPrincipal principal) {
        Juego existente = juegoService.buscarPorId(id);
        verificarPropietarioOAdmin(existente, principal);
        Seccion seccion = buscarSeccion(form.getSeccionId());
        Juego actualizado = juegoService.actualizar(id, form.getNombre(), form.getDescripcion(),
                form.getPrecio(), form.getStock(), seccion, imagen);
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

    /** Reposicion de stock: suma {@code cantidad} unidades. Vendedor dueno o admin. */
    @PostMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public JuegoDto agregarStock(@PathVariable Long id,
                                 @RequestParam @Positive int cantidad,
                                 @AuthenticationPrincipal AuthPrincipal principal) {
        Juego juego = juegoService.buscarPorId(id);
        verificarPropietarioOAdmin(juego, principal);
        return mapper.aDto(juegoService.agregarStock(id, cantidad), principal);
    }

    private Seccion buscarSeccion(Long seccionId) {
        return seccionId != null ? seccionRepository.findById(seccionId).orElse(null) : null;
    }

    private void verificarPropietarioOAdmin(Juego juego, AuthPrincipal principal) {
        if (!juegoService.puedeGestionar(juego, principal)) {
            throw new AccessDeniedException("No podes modificar juegos de otro vendedor");
        }
    }
}
