package com.example.tiendajuegos.service;

import com.example.tiendajuegos.model.Juego;
import com.example.tiendajuegos.model.Rol;
import com.example.tiendajuegos.model.Seccion;
import com.example.tiendajuegos.model.Usuario;
import com.example.tiendajuegos.repository.JuegoRepository;
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

    public List<Juego> listarDeVendedor(Usuario vendedor) {
        return juegoRepository.findByVendedor(vendedor);
    }

    @Transactional
    public Juego crear(String nombre, String descripcion, BigDecimal precio, Seccion seccion,
                        Usuario vendedor, MultipartFile imagen) {
        Juego juego = new Juego();
        juego.setNombre(nombre);
        juego.setDescripcion(descripcion);
        juego.setPrecio(precio);
        juego.setSeccion(seccion);
        juego.setVendedor(vendedor);
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
     * Regla de negocio de autorizacion a nivel de dato (no solo de URL):
     * un VENDEDOR unicamente puede tocar sus propios juegos; el ADMIN
     * puede moderar cualquier juego de cualquier vendedor.
     */
    public boolean puedeGestionar(Juego juego, Usuario usuarioActual) {
        if (usuarioActual.getRol() == Rol.ADMIN) {
            return true;
        }
        return usuarioActual.getRol() == Rol.VENDEDOR
                && juego.getVendedor().getId().equals(usuarioActual.getId());
    }
}
