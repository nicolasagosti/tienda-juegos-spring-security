package com.example.tiendajuegos.controller;

import com.example.tiendajuegos.model.Juego;
import com.example.tiendajuegos.model.Rol;
import com.example.tiendajuegos.model.Seccion;
import com.example.tiendajuegos.model.Usuario;
import com.example.tiendajuegos.repository.SeccionRepository;
import com.example.tiendajuegos.security.CustomUserDetails;
import com.example.tiendajuegos.service.JuegoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * Catalogo de juegos.
 *
 * - GET  /juegos            -> lo ve cualquier usuario autenticado (COMPRADOR incluido).
 * - GET  /juegos/nuevo      -> solo VENDEDOR o ADMIN (reforzado con @PreAuthorize).
 * - POST /juegos/guardar    -> solo VENDEDOR o ADMIN.
 * - editar/eliminar         -> solo el VENDEDOR dueño del juego, o cualquier ADMIN
 *                               (esa comparacion "es el dueño" no se puede expresar
 *                               solo con URLs, por eso se valida aca en el controller
 *                               ademas de con @PreAuthorize a nivel de rol).
 */
@Controller
@RequestMapping("/juegos")
public class JuegoController {

    private final JuegoService juegoService;
    private final SeccionRepository seccionRepository;

    public JuegoController(JuegoService juegoService, SeccionRepository seccionRepository) {
        this.juegoService = juegoService;
        this.seccionRepository = seccionRepository;
    }

    @GetMapping
    public String listar(Model model, @AuthenticationPrincipal CustomUserDetails principal) {
        model.addAttribute("juegos", juegoService.listarTodos());
        model.addAttribute("usuarioActual", principal.getUsuario());
        return "juegos/list";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public String formularioNuevo(Model model) {
        model.addAttribute("juego", new Juego());
        model.addAttribute("secciones", seccionRepository.findAll());
        model.addAttribute("esNuevo", true);
        return "juegos/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public String guardarNuevo(@RequestParam String nombre,
                                @RequestParam(required = false) String descripcion,
                                @RequestParam BigDecimal precio,
                                @RequestParam(required = false) Long seccionId,
                                @RequestParam(required = false) MultipartFile imagen,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                RedirectAttributes redirectAttributes) {
        Seccion seccion = (seccionId != null) ? seccionRepository.findById(seccionId).orElse(null) : null;
        Usuario vendedor = principal.getUsuario();
        juegoService.crear(nombre, descripcion, precio, seccion, vendedor, imagen);
        redirectAttributes.addFlashAttribute("mensaje", "Juego publicado correctamente");
        return "redirect:/juegos";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public String formularioEditar(@PathVariable Long id, Model model,
                                    @AuthenticationPrincipal CustomUserDetails principal) {
        Juego juego = juegoService.buscarPorId(id);
        verificarPropietarioOAdmin(juego, principal.getUsuario());
        model.addAttribute("juego", juego);
        model.addAttribute("secciones", seccionRepository.findAll());
        model.addAttribute("esNuevo", false);
        return "juegos/form";
    }

    @PostMapping("/{id}/guardar")
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public String guardarEdicion(@PathVariable Long id,
                                  @RequestParam String nombre,
                                  @RequestParam(required = false) String descripcion,
                                  @RequestParam BigDecimal precio,
                                  @RequestParam(required = false) Long seccionId,
                                  @RequestParam(required = false) MultipartFile imagen,
                                  @AuthenticationPrincipal CustomUserDetails principal,
                                  RedirectAttributes redirectAttributes) {
        Juego juego = juegoService.buscarPorId(id);
        verificarPropietarioOAdmin(juego, principal.getUsuario());
        Seccion seccion = (seccionId != null) ? seccionRepository.findById(seccionId).orElse(null) : null;
        juegoService.actualizar(id, nombre, descripcion, precio, seccion, imagen);
        redirectAttributes.addFlashAttribute("mensaje", "Juego actualizado correctamente");
        return "redirect:/juegos";
    }

    @PostMapping("/{id}/eliminar")
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public String eliminar(@PathVariable Long id,
                            @AuthenticationPrincipal CustomUserDetails principal,
                            RedirectAttributes redirectAttributes) {
        Juego juego = juegoService.buscarPorId(id);
        verificarPropietarioOAdmin(juego, principal.getUsuario());
        juegoService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Juego eliminado");
        return "redirect:/juegos";
    }

    private void verificarPropietarioOAdmin(Juego juego, Usuario usuarioActual) {
        if (usuarioActual.getRol() != Rol.ADMIN
                && !juego.getVendedor().getId().equals(usuarioActual.getId())) {
            throw new AccessDeniedException("No podes modificar juegos de otro vendedor");
        }
    }
}
