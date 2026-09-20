package com.gamestore.auth.dto;

/** Igual que el UsuarioDTO del monolito; se arma con datos que trae negocio-service. */
public record UsuarioDto(Long id, String username, String nombreCompleto, String email, String rol, boolean habilitado) {
}
