package com.example.tiendajuegos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * Pagina de login "clasica" en Thymeleaf. Sigue viva para quien quiera
     * comparar el flujo con formulario + redirect, pero la experiencia
     * principal ahora es la SPA de React que vive en "/" (ver nota abajo).
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * OJO: a proposito NO hay @GetMapping("/") aca. La raiz "/" la sirve
     * Spring Boot como recurso estatico (index.html generado por
     * "npm run build" en /frontend, copiado a resources/static): es el
     * punto de entrada de la SPA de React. Si esta clase mapeara "/",
     * el controller le ganaria al recurso estatico y el React nunca
     * llegaria a cargarse.
     */
    @GetMapping("/inicio")
    public String inicio() {
        return "redirect:/juegos";
    }

    /** Pagina mostrada cuando Spring Security bloquea el acceso (403) en la UI clasica de Thymeleaf. */
    @GetMapping("/403")
    public String accesoDenegado() {
        return "error/403";
    }
}
