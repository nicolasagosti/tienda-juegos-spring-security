package com.gamestore.negocio.catalogo.web;

import com.gamestore.common.security.AuthPrincipal;
import com.gamestore.negocio.catalogo.model.Juego;
import com.gamestore.negocio.catalogo.service.CompraService;
import com.gamestore.negocio.catalogo.service.JuegoService;
import com.gamestore.negocio.catalogo.spi.ResolucionVendedores;
import com.gamestore.negocio.catalogo.web.dto.JuegoDto;
import com.gamestore.negocio.catalogo.web.dto.SeccionDto;
import com.gamestore.negocio.catalogo.web.dto.VendedorDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Arma los {@link JuegoDto}: resuelve los vendedores en UNA sola pasada
 * ({@link ResolucionVendedores}, ahora en memoria) y calcula
 * {@code puedeEditar} / {@code comprado} con el username/rol del JWT, del
 * lado del servidor.
 */
@Component
public class JuegoDtoMapper {

    private final ResolucionVendedores resolucionVendedores;
    private final JuegoService juegoService;
    private final CompraService compraService;

    public JuegoDtoMapper(ResolucionVendedores resolucionVendedores, JuegoService juegoService,
                          CompraService compraService) {
        this.resolucionVendedores = resolucionVendedores;
        this.juegoService = juegoService;
        this.compraService = compraService;
    }

    public List<JuegoDto> aDtos(List<Juego> juegos, AuthPrincipal principal) {
        List<String> usernames = juegos.stream()
                .map(Juego::getVendedorUsername)
                .distinct()
                .toList();
        Map<String, ResolucionVendedores.Vendedor> vendedores = resolucionVendedores.porUsernames(usernames);
        Set<Long> comprados = comprados(principal);
        return juegos.stream().map(j -> aDto(j, vendedores, comprados, principal)).toList();
    }

    public JuegoDto aDto(Juego j, AuthPrincipal principal) {
        return aDto(j, resolucionVendedores.porUsernames(List.of(j.getVendedorUsername())), comprados(principal), principal);
    }

    /** Solo un COMPRADOR "compra": para los demas roles la marca no aplica. */
    private Set<Long> comprados(AuthPrincipal principal) {
        if (principal == null || !"COMPRADOR".equals(principal.rol())) {
            return Set.of();
        }
        return compraService.juegosComprados(principal.username());
    }

    private JuegoDto aDto(Juego j, Map<String, ResolucionVendedores.Vendedor> vendedores,
                          Set<Long> comprados, AuthPrincipal principal) {
        ResolucionVendedores.Vendedor vendedor = vendedores.getOrDefault(
                j.getVendedorUsername(), ResolucionVendedores.Vendedor.degradado(j.getVendedorUsername()));
        return new JuegoDto(
                j.getId(),
                j.getNombre(),
                j.getDescripcion(),
                j.getPrecio(),
                j.getStock(),
                j.getImagenUrl(),
                SeccionDto.from(j.getSeccion()),
                VendedorDto.from(vendedor),
                juegoService.puedeGestionar(j, principal),
                comprados.contains(j.getId()));
    }
}
