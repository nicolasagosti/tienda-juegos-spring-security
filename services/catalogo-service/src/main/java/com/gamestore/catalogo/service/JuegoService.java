package com.gamestore.catalogo.service;

import com.gamestore.catalogo.model.Juego;
import com.gamestore.catalogo.model.Seccion;
import com.gamestore.catalogo.repository.JuegoRepository;
import com.gamestore.common.security.AuthPrincipal;
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
    public Juego crear(String nombre, String descripcion, BigDecimal precio, Seccion seccion,
                       String vendedorUsername, MultipartFile imagen) {
        Juego juego = new Juego();
        juego.setNombre(nombre);
        juego.setDescripcion(descripcion);
        juego.setPrecio(precio);
        juego.setSeccion(seccion);
        juego.setVendedorUsername(vendedorUsername);
        String url = imagenStorageService.guardar(imagen);
        if (url != null) {
            juego.setImagenUrl(url);
        }
        return juegoRepository.save(juego);
    }

    @Transactional
    public Juego actualizar(Long id, String nombre, String descripcion, BigDecimal precio,
                            Seccion seccion, MultipartFile imagen) {
        Juego juego = buscarPorId(id);
        juego.setNombre(nombre);
        juego.setDescripcion(descripcion);
        juego.setPrecio(precio);
        juego.setSeccion(seccion);
        String url = imagenStorageService.guardar(imagen);
        if (url != null) {
            juego.setImagenUrl(url);
        }
        return juegoRepository.save(juego);
    }

    @Transactional
    public void eliminar(Long id) {
        juegoRepository.deleteById(id);
    }

    /**
     * Autorizacion a nivel de dato: el ADMIN modera cualquier juego; el
     * VENDEDOR solo los suyos. Se compara contra el username del JWT (ya no
     * hay entidad Usuario que consultar).
     */
    public boolean puedeGestionar(Juego juego, AuthPrincipal usuarioActual) {
        return usuarioActual.isAdmin()
                || juego.getVendedorUsername().equals(usuarioActual.username());
    }
}
