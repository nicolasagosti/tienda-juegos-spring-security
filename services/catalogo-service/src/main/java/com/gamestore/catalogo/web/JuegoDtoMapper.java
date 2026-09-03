package com.gamestore.catalogo.web;

import com.gamestore.catalogo.client.UsuariosClient;
import com.gamestore.catalogo.model.Juego;
import com.gamestore.catalogo.service.JuegoService;
import com.gamestore.catalogo.web.Dtos.JuegoDTO;
import com.gamestore.catalogo.web.Dtos.SeccionDTO;
import com.gamestore.catalogo.web.Dtos.VendedorDTO;
import com.gamestore.common.security.AuthPrincipal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Arma los JuegoDTO: resuelve los vendedores en UNA sola llamada a
 * usuarios-service (no N) y calcula {@code puedeEditar} con el username/rol
 * del JWT, del lado del servidor (el frontend no decide esto).
 */
@Component
public class JuegoDtoMapper {

    private final UsuariosClient usuariosClient;
    private final JuegoService juegoService;

    public JuegoDtoMapper(UsuariosClient usuariosClient, JuegoService juegoService) {
        this.usuariosClient = usuariosClient;
        this.juegoService = juegoService;
    }

    public List<JuegoDTO> aDtos(List<Juego> juegos, AuthPrincipal principal) {
        List<String> usernames = juegos.stream()
                .map(Juego::getVendedorUsername)
                .distinct()
                .toList();
        Map<String, VendedorDTO> vendedores = usuariosClient.porUsernames(usernames);
        return juegos.stream().map(j -> aDto(j, vendedores, principal)).toList();
    }

    public JuegoDTO aDto(Juego j, AuthPrincipal principal) {
        return aDto(j, usuariosClient.porUsernames(List.of(j.getVendedorUsername())), principal);
    }

    private JuegoDTO aDto(Juego j, Map<String, VendedorDTO> vendedores, AuthPrincipal principal) {
        VendedorDTO vendedor = vendedores.getOrDefault(
                j.getVendedorUsername(), UsuariosClient.degradado(j.getVendedorUsername()));
        return new JuegoDTO(
                j.getId(),
                j.getNombre(),
                j.getDescripcion(),
                j.getPrecio(),
                j.getImagenUrl(),
                SeccionDTO.from(j.getSeccion()),
                vendedor,
                juegoService.puedeGestionar(j, principal));
    }
}
