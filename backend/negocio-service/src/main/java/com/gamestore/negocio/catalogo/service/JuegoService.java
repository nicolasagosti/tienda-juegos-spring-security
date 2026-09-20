package com.gamestore.negocio.catalogo.service;

import com.gamestore.common.security.AuthPrincipal;
import com.gamestore.negocio.catalogo.model.Juego;
import com.gamestore.negocio.catalogo.model.Seccion;
import com.gamestore.negocio.catalogo.repository.JuegoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Service
public class JuegoService {

    private final JuegoRepository juegoRepository;
    private final ImagenStorageService imagenStorageService;

    public JuegoService(JuegoRepository juegoRepository, ImagenStorageService imagenStorageService) {
        this.juegoRepository = juegoRepository;
        this.imagenStorageService = imagenStorageService;
    }

    public List<Juego> listarTodos() {
        return juegoRepository.findAll();
    }

    public Juego buscarPorId(Long id) {
        return juegoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado: " + id));
    }

    public long contarDeVendedor(String vendedorUsername) {
        return juegoRepository.countByVendedorUsername(vendedorUsername);
    }

    @Transactional
    public Juego crear(String nombre, String descripcion, BigDecimal precio, int stock, Seccion seccion,
                       String vendedorUsername, MultipartFile imagen) {
        Juego juego = new Juego();
        juego.setNombre(nombre);
        juego.setDescripcion(descripcion);
        juego.setPrecio(precio);
        juego.setStock(Math.max(0, stock));
        juego.setSeccion(seccion);
        juego.setVendedorUsername(vendedorUsername);
        String url = imagenStorageService.guardar(imagen);
        if (url != null) {
            juego.setImagenUrl(url);
        }
        return juegoRepository.save(juego);
    }

    @Transactional
    public Juego actualizar(Long id, String nombre, String descripcion, BigDecimal precio, int stock,
                            Seccion seccion, MultipartFile imagen) {
        Juego juego = buscarPorId(id);
        juego.setNombre(nombre);
        juego.setDescripcion(descripcion);
        juego.setPrecio(precio);
        juego.setStock(Math.max(0, stock));
        juego.setSeccion(seccion);
        String url = imagenStorageService.guardar(imagen);
        if (url != null) {
            juego.setImagenUrl(url);
        }
        return juegoRepository.save(juego);
    }

    /**
     * Descuenta una unidad de stock, con lock de escritura sobre la fila.
     * Lo usa {@link CompraService} al concretar una compra.
     */
    @Transactional
    public Juego descontarStock(Long juegoId) {
        Juego juego = juegoRepository.findByIdParaActualizar(juegoId)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado: " + juegoId));
        if (juego.getStock() <= 0) {
            throw new IllegalArgumentException("No hay stock disponible de este juego");
        }
        juego.setStock(juego.getStock() - 1);
        return juegoRepository.save(juego);
    }

    /**
     * Suma unidades al stock (reposicion). La verificacion de que quien
     * llama sea el vendedor dueno o un admin se hace en el controller.
     */
    @Transactional
    public Juego agregarStock(Long juegoId, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a agregar debe ser mayor a 0");
        }
        Juego juego = juegoRepository.findByIdParaActualizar(juegoId)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado: " + juegoId));
        juego.setStock(juego.getStock() + cantidad);
        return juegoRepository.save(juego);
    }

    @Transactional
    public void eliminar(Long id) {
        juegoRepository.deleteById(id);
    }

    /**
     * Autorizacion a nivel de dato: el ADMIN modera cualquier juego; el
     * VENDEDOR solo los suyos. Se compara contra el username del JWT.
     */
    public boolean puedeGestionar(Juego juego, AuthPrincipal usuarioActual) {
        return usuarioActual.isAdmin()
                || juego.getVendedorUsername().equals(usuarioActual.username());
    }
}
