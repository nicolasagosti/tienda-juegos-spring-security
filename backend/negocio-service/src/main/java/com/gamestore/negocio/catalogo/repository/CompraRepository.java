package com.gamestore.negocio.catalogo.repository;

import com.gamestore.negocio.catalogo.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    /** Las compras de un usuario, de la mas nueva a la mas vieja (para "Mis compras"). */
    List<Compra> findByCompradorUsernameOrderByFechaCompraDesc(String compradorUsername);

    /** Todas las compras de un usuario (para marcar el catalogo). */
    List<Compra> findByCompradorUsername(String compradorUsername);

    /** Un comprador no puede comprar dos veces el mismo juego. */
    boolean existsByJuegoIdAndCompradorUsername(Long juegoId, String compradorUsername);
}
