package com.gamestore.negocio.usuarios.service;

import com.gamestore.negocio.usuarios.client.AuthClient;
import com.gamestore.negocio.usuarios.model.Rol;
import com.gamestore.negocio.usuarios.model.Usuario;
import com.gamestore.negocio.usuarios.repository.UsuarioRepository;
import com.gamestore.negocio.usuarios.spi.ConsultaCatalogo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository repo;
    @Mock
    AuthClient authClient;
    @Mock
    ConsultaCatalogo catalogo;

    @InjectMocks
    UsuarioService usuarioService;

    private static Usuario usuario(long id, String username, Rol rol) {
        Usuario u = new Usuario(username, "Nombre " + username, username + "@x.com", rol);
        u.setHabilitado(true);
        // id lo asigna la base; para el test alcanza con dejarlo en null salvo que importe.
        return u;
    }

    // ---------- alta ----------

    @Test
    void crearUsuario_username_repetido_falla_sin_tocar_auth() {
        when(repo.existsByUsername("admin")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crearUsuario("admin", "secret1", "Admin", "a@x.com", Rol.ADMIN))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(authClient);
        verify(repo, never()).saveAndFlush(any());
    }

    @Test
    void crearUsuario_email_repetido_falla() {
        when(repo.existsByUsername("nuevo")).thenReturn(false);
        when(repo.existsByEmail("dup@x.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crearUsuario("nuevo", "secret1", "Nuevo", "dup@x.com", Rol.COMPRADOR))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void crearUsuario_ok_persiste_perfil_y_delega_credencial() {
        when(repo.existsByUsername("nuevo")).thenReturn(false);
        when(repo.existsByEmail("n@x.com")).thenReturn(false);
        when(repo.saveAndFlush(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.crearUsuario("nuevo", "secret1", "Nuevo", "n@x.com", Rol.VENDEDOR);

        verify(authClient).crearCredencial("nuevo", "n@x.com", "secret1");
    }

    @Test
    void crearUsuario_si_auth_falla_compensa_borrando_el_perfil() {
        when(repo.existsByUsername("nuevo")).thenReturn(false);
        when(repo.existsByEmail(any())).thenReturn(false);
        Usuario guardado = usuario(1L, "nuevo", Rol.VENDEDOR);
        when(repo.saveAndFlush(any(Usuario.class))).thenReturn(guardado);
        doThrow(new RuntimeException("auth-service caido"))
                .when(authClient).crearCredencial(anyString(), any(), anyString());

        assertThatThrownBy(() -> usuarioService.crearUsuario("nuevo", "secret1", "Nuevo", "n@x.com", Rol.VENDEDOR))
                .isInstanceOf(IllegalStateException.class);

        verify(repo).delete(guardado);
    }

    // ---------- edicion ----------

    @Test
    void actualizarUsuario_sin_password_no_llama_a_auth() {
        Usuario existente = usuario(5L, "vendedor1", Rol.VENDEDOR);
        when(repo.findById(5L)).thenReturn(Optional.of(existente));

        usuarioService.actualizarUsuario(5L, "Nuevo Nombre", "otro@x.com", Rol.ADMIN, false, "  ");

        assertThat(existente.getNombreCompleto()).isEqualTo("Nuevo Nombre");
        assertThat(existente.getRol()).isEqualTo(Rol.ADMIN);
        assertThat(existente.isHabilitado()).isFalse();
        verify(authClient, never()).cambiarPassword(any(), any());
    }

    @Test
    void actualizarUsuario_con_password_la_delega_a_auth() {
        Usuario existente = usuario(5L, "vendedor1", Rol.VENDEDOR);
        when(repo.findById(5L)).thenReturn(Optional.of(existente));

        usuarioService.actualizarUsuario(5L, "V1", "v1@x.com", Rol.VENDEDOR, true, "nuevaClave1");

        verify(authClient).cambiarPassword("vendedor1", "nuevaClave1");
    }

    @Test
    void alternarHabilitado_invierte_el_flag() {
        Usuario u = usuario(3L, "comprador1", Rol.COMPRADOR);
        u.setHabilitado(true);
        when(repo.findById(3L)).thenReturn(Optional.of(u));

        usuarioService.alternarHabilitado(3L);

        assertThat(u.isHabilitado()).isFalse();
    }

    // ---------- baja ----------

    @Test
    void eliminarUsuario_con_juegos_publicados_tira_conflict_y_no_borra() {
        Usuario u = usuario(7L, "vendedor1", Rol.VENDEDOR);
        when(repo.findById(7L)).thenReturn(Optional.of(u));
        when(catalogo.juegosDeVendedor("vendedor1")).thenReturn(3L);

        assertThatThrownBy(() -> usuarioService.eliminarUsuario(7L))
                .isInstanceOf(ConflictException.class);

        verify(repo, never()).delete(any());
        verifyNoInteractions(authClient);
    }

    @Test
    void eliminarUsuario_sin_juegos_borra_perfil_y_credencial() {
        Usuario u = usuario(7L, "comprador1", Rol.COMPRADOR);
        when(repo.findById(7L)).thenReturn(Optional.of(u));
        when(catalogo.juegosDeVendedor("comprador1")).thenReturn(0L);

        usuarioService.eliminarUsuario(7L);

        verify(repo).delete(u);
        verify(authClient).eliminarCredencial("comprador1");
    }

    // ---------- login con Google ----------

    @Test
    void buscarOCrearDesdeGoogle_reutiliza_el_usuario_existente_por_email() {
        Usuario existente = usuario(1L, "juanp", Rol.COMPRADOR);
        when(repo.findByEmail("juanp@gmail.com")).thenReturn(Optional.of(existente));

        Usuario resultado = usuarioService.buscarOCrearDesdeGoogle("juanp@gmail.com", "Juan Perez");

        assertThat(resultado).isSameAs(existente);
        verify(repo, never()).save(any());
    }

    @Test
    void buscarOCrearDesdeGoogle_crea_comprador_con_username_libre() {
        when(repo.findByEmail("nuevo@gmail.com")).thenReturn(Optional.empty());
        when(repo.existsByUsername("nuevo")).thenReturn(true);
        when(repo.existsByUsername("nuevo1")).thenReturn(false);
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario creado = usuarioService.buscarOCrearDesdeGoogle("nuevo@gmail.com", "Nombre Nuevo");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("nuevo1");
        assertThat(captor.getValue().getRol()).isEqualTo(Rol.COMPRADOR);
        assertThat(creado.getNombreCompleto()).isEqualTo("Nombre Nuevo");
    }
}
