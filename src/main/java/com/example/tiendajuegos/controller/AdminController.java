package com.example.tiendajuegos.controller;

import com.example.tiendajuegos.model.Rol;
import com.example.tiendajuegos.model.Usuario;
import com.example.tiendajuegos.security.CustomUserDetails;
import com.example.tiendajuegos.service.JuegoService;
import com.example.tiendajuegos.service.SeccionService;
import com.example.tiendajuegos.service.UsuarioService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Panel exclusivo del ADMIN.
 *
 * Doble candado, a proposito, para que se vea la diferencia:
 *  - SecurityConfig ya bloquea /admin/** a nivel de URL (hasRole ADMIN).
 *  - Aca ademas se pone @PreAuthorize a nivel de clase/metodo, que es la
 *    forma "defensa en profundidad": si mañana alguien reusa este metodo
 *    desde otra ruta sin querer, la regla de negocio sigue protegida.
 *
 * Funciones:
 *  - Crear usuarios y asignarles categoria (rol).
 *  - Editar el perfil de cualquier usuario (nombre, email, rol, password).
 *  - Habilitar/deshabilitar cuentas (extra) y eliminarlas.
 *  - Crear y eliminar secciones (categorias del catalogo).
 *  - Dashboard con estadisticas (extra) y moderacion de juegos (usa el
 *    mismo endpoint de borrado de JuegoController, al que el ADMIN ya
 *    tiene acceso total).
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UsuarioService usuarioService;
    private final SeccionService seccionService;
    private final JuegoService juegoService;

    public AdminController(UsuarioService usuarioService, SeccionService seccionService, JuegoService juegoService) {
        this.usuarioService = usuarioService;
        this.seccionService = seccionService;
        this.juegoService = juegoService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalUsuarios", usuarioService.listarTodos().size());
        model.addAttribute("totalAdmins", usuarioService.contarPorRol(Rol.ADMIN));
        model.addAttribute("totalVendedores", usuarioService.contarPorRol(Rol.VENDEDOR));
        model.addAttribute("totalCompradores", usuarioService.contarPorRol(Rol.COMPRADOR));
        model.addAttribute("totalJuegos", juegoService.listarTodos().size());
        model.addAttribute("totalSecciones", seccionService.listarTodas().size());
        return "admin/dashboard";
    }

    // ---------- Gestion de usuarios ----------

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "admin/usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String formularioNuevoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", Rol.values());
        model.addAttribute("esNuevo", true);
        return "admin/usuario-form";
    }

    @PostMapping("/usuarios/guardar")
    public String crearUsuario(@RequestParam String username,
                                @RequestParam String password,
                                @RequestParam String nombreCompleto,
                                @RequestParam String email,
                                @RequestParam Rol rol,
                                RedirectAttributes redirectAttributes) {
        try {
            usuarioService.crearUsuario(username, password, nombreCompleto, email, rol);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado y categoria asignada: " + rol);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String formularioEditarUsuario(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioService.buscarPorId(id));
        model.addAttribute("roles", Rol.values());
        model.addAttribute("esNuevo", false);
        return "admin/usuario-form";
    }

    @PostMapping("/usuarios/{id}/guardar")
    public String actualizarUsuario(@PathVariable Long id,
                                     @RequestParam String nombreCompleto,
                                     @RequestParam String email,
                                     @RequestParam Rol rol,
                                     @RequestParam(required = false, defaultValue = "false") boolean habilitado,
                                     @RequestParam(required = false) String nuevaPassword,
                                     RedirectAttributes redirectAttributes) {
        usuarioService.actualizarUsuario(id, nombreCompleto, email, rol, habilitado, nuevaPassword);
        redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/toggle")
    public String alternarHabilitado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.alternarHabilitado(id);
        redirectAttributes.addFlashAttribute("mensaje", "Estado de la cuenta actualizado");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/eliminar")
    public String eliminarUsuario(@PathVariable Long id,
                                   @AuthenticationPrincipal CustomUserDetails principal,
                                   RedirectAttributes redirectAttributes) {
        if (principal.getUsuario().getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "No podes eliminar tu propio usuario");
            return "redirect:/admin/usuarios";
        }
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error",
                    "No se puede eliminar: el usuario tiene juegos publicados. Eliminalos primero.");
        }
        return "redirect:/admin/usuarios";
    }

    // ---------- Gestion de secciones (categorias del catalogo) ----------

    @GetMapping("/secciones")
    public String listarSecciones(Model model) {
        model.addAttribute("secciones", seccionService.listarTodas());
        return "admin/secciones";
    }

    @PostMapping("/secciones/guardar")
    public String crearSeccion(@RequestParam String nombre,
                                @RequestParam(required = false) String descripcion,
                                RedirectAttributes redirectAttributes) {
        try {
            seccionService.crear(nombre, descripcion);
            redirectAttributes.addFlashAttribute("mensaje", "Seccion creada");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/secciones";
    }

    @PostMapping("/secciones/{id}/eliminar")
    public String eliminarSeccion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            seccionService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Seccion eliminada");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error",
                    "No se puede eliminar: hay juegos que pertenecen a esta seccion.");
        }
        return "redirect:/admin/secciones";
    }
}
