package com.gamestore.negocio.catalogo.service;

import com.gamestore.common.security.AuthPrincipal;
import com.gamestore.negocio.catalogo.model.Compra;
import com.gamestore.negocio.catalogo.model.Juego;
import com.gamestore.negocio.catalogo.repository.CompraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Compra ficticia: no hay pago, solo deja constancia de que un COMPRADOR se
 * quedo con un juego. Un mismo comprador no puede comprar dos veces el mismo
 * juego (lo bloquea este chequeo y la constraint de la tabla).
 */
@Service
public class CompraService {

    private final CompraRepository compraRepository;
    private final JuegoService juegoService;

    public CompraService(CompraRepository compraRepository, JuegoService juegoService) {
        this.compraRepository = compraRepository;
        this.juegoService = juegoService;
    }

    @Transactional
    public Compra comprar(Long juegoId, AuthPrincipal comprador) {
        if (compraRepository.existsByJuegoIdAndCompradorUsername(juegoId, comprador.username())) {
            throw new IllegalArgumentException("Ya compraste este juego");
        }
        // Descuenta 1 unidad con lock de fila: si no hay stock, tira 400 y no
        // se crea la compra (misma transaccion).
        Juego juego = juegoService.descontarStock(juegoId);
        Compra compra = new Compra();
        compra.setJuegoId(juego.getId());
        compra.setNombreJuego(juego.getNombre());
        compra.setImagenUrl(juego.getImagenUrl());
        compra.setPrecio(juego.getPrecio());
        compra.setCompradorUsername(comprador.username());
        return compraRepository.save(compra);
    }

    public List<Compra> misCompras(String compradorUsername) {
        return compraRepository.findByCompradorUsernameOrderByFechaCompraDesc(compradorUsername);
    }

    /** Ids de juegos que este comprador ya compro, para marcar el catalogo. */
    public Set<Long> juegosComprados(String compradorUsername) {
        return compraRepository.findByCompradorUsername(compradorUsername).stream()
                .map(Compra::getJuegoId)
                .collect(Collectors.toSet());
    }
}
