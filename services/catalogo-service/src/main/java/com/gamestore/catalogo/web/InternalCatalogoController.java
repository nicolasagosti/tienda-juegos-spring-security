package com.gamestore.catalogo.web;

import com.gamestore.catalogo.repository.JuegoRepository;
import com.gamestore.catalogo.repository.SeccionRepository;
import com.gamestore.catalogo.service.JuegoService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API interna. La usa usuarios-service para el dashboard (stats) y para
 * verificar si un usuario tiene juegos antes de borrarlo.
 */
@RestController
@RequestMapping("/internal")
public class InternalCatalogoController {

    private final JuegoRepository juegoRepository;
    private final SeccionRepository seccionRepository;
    private final JuegoService juegoService;

    public InternalCatalogoController(JuegoRepository juegoRepository,
                                      SeccionRepository seccionRepository,
                                      JuegoService juegoService) {
        this.juegoRepository = juegoRepository;
        this.seccionRepository = seccionRepository;
        this.juegoService = juegoService;
    }

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return Map.of(
                "totalJuegos", juegoRepository.count(),
                "totalSecciones", seccionRepository.count());
    }

    @GetMapping("/juegos/count-by-vendedor/{username}")
    public long countByVendedor(@PathVariable String username) {
        return juegoService.contarDeVendedor(username);
    }
}
