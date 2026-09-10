package com.gamestore.negocio.usuarios.web.dto;

/** Totales del dashboard del ADMIN (usuarios locales + catalogo en memoria). */
public record StatsDto(long totalUsuarios, long totalAdmins, long totalVendedores,
                       long totalCompradores, long totalJuegos, long totalSecciones) {
}
