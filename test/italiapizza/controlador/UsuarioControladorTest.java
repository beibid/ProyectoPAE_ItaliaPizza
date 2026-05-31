package italiapizza.controlador;


import italiapizza.modelo.Usuario;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


class UsuarioControladorTest {

    private final UsuarioControlador usuarioControlador = new UsuarioControlador();

    private Usuario crearClienteValido() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(10);
        usuario.setNombre("María");
        usuario.setApellidos("López García");
        usuario.setTelefono("2281234567");
        usuario.setTipo(Usuario.Tipo.CLIENTE);
        return usuario;
    }

    private Usuario crearEmpleadoValido() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(20);
        usuario.setNombre("Carlos");
        usuario.setApellidos("Martínez");
        usuario.setTelefono("2289876543");
        usuario.setTipo(Usuario.Tipo.EMPLEADO);
        usuario.setUsername("carlos.m");
        usuario.setPassword("segura123");
        usuario.setRol(Usuario.Rol.CAJERO);
        return usuario;
    }

    @Test
    void registrar_conClienteValido_retornaNull() {
        String resultado = usuarioControlador.registrar(crearClienteValido());
        assertNull(resultado);
    }

    @Test
    void registrar_conEmpleadoValido_retornaNull() {
        String resultado = usuarioControlador.registrar(crearEmpleadoValido());
        assertNull(resultado);
    }

    @Test
    void registrar_conNombreVacio_retornaMensajeDeError() {
        Usuario usuario = crearClienteValido();
        usuario.setNombre("");
        String resultado = usuarioControlador.registrar(usuario);
        assertNotNull(resultado);
    }

    @Test
    void registrar_conApellidosVacios_retornaMensajeDeError() {
        Usuario usuario = crearClienteValido();
        usuario.setApellidos("  ");
        String resultado = usuarioControlador.registrar(usuario);
        assertNotNull(resultado);
    }

    @Test
    void registrar_conTelefonoVacio_retornaMensajeDeError() {
        Usuario usuario = crearClienteValido();
        usuario.setTelefono("");
        String resultado = usuarioControlador.registrar(usuario);
        assertNotNull(resultado);
    }

    @Test
    void registrar_conEmpleadoSinUsername_retornaMensajeDeError() {
        Usuario usuario = crearEmpleadoValido();
        usuario.setUsername("");
        String resultado = usuarioControlador.registrar(usuario);
        assertNotNull(resultado);
    }

    @Test
    void registrar_conEmpleadoSinRol_retornaMensajeDeError() {
        Usuario usuario = crearEmpleadoValido();
        usuario.setRol(null);
        String resultado = usuarioControlador.registrar(usuario);
        assertNotNull(resultado);
    }

    @Test
    void eliminar_usuarioEnSesionActual_retornaMensajeDeError() {
        Usuario usuario = crearClienteValido();
        String resultado = usuarioControlador.eliminar(usuario, usuario.getIdUsuario());
        assertNotNull(resultado);
    }

    @Test
    void obtenerTodos_retornaListaNoNula() {
        List<Usuario> resultado = usuarioControlador.obtenerTodos();
        assertNotNull(resultado);
    }

    @Test
    void obtenerClientes_retornaListaNoNula() {
        List<Usuario> resultado = usuarioControlador.obtenerClientes();
        assertNotNull(resultado);
    }

}
