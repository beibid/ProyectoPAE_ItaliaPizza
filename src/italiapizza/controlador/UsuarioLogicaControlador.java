package italiapizza.controlador;

import italiapizza.dao.impl.UsuarioDaoImpl;
import italiapizza.excepcion.DaoException;
import italiapizza.excepcion.UsuarioNoEliminableException;
import italiapizza.modelo.Usuario;

import java.util.Collections;
import java.util.List;

public class UsuarioLogicaControlador {

    private final UsuarioDaoImpl usuarioDao = new UsuarioDaoImpl();

    public String registrar(Usuario usuario) {
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            usuario.setEmail("usuario_" + System.currentTimeMillis() + "@gmail.com");
        }
        String mensajeValidacion = validarUsuario(usuario);
        if (mensajeValidacion != null) return mensajeValidacion;
        try {
            usuarioDao.registrar(usuario);
            return null;
        } catch (DaoException e) {
            return "Error al registrar usuario: " + e.getMessage();
        }
    }

    public String actualizar(Usuario usuario) {
        String mensajeValidacion = validarUsuario(usuario);
        if (mensajeValidacion != null) return mensajeValidacion;
        try {
            usuarioDao.actualizar(usuario);
            return null;
        } catch (DaoException e) {
            return "Error al actualizar usuario: " + e.getMessage();
        }
    }

    public String eliminar(Usuario usuario, int idUsuarioEnSesion) {
        if (usuario.getIdUsuario() == idUsuarioEnSesion) {
            return new UsuarioNoEliminableException(
                    "No puedes eliminar tu propia cuenta.").getMessage();
        }
        try {
            if (usuario.getTipo() == Usuario.Tipo.CLIENTE
                    && usuarioDao.tienePedidos(usuario.getIdUsuario())) {
                return new UsuarioNoEliminableException(
                        "El cliente tiene pedidos registrados.").getMessage();
            }
            usuarioDao.eliminar(usuario.getIdUsuario());
            return null;
        } catch (DaoException e) {
            return "Error al eliminar usuario: " + e.getMessage();
        }
    }

    public List<Usuario> obtenerTodos() {
        try {
            return usuarioDao.buscarTodos();
        } catch (DaoException e) {
            return Collections.emptyList();
        }
    }

    public List<Usuario> obtenerClientes() {
        try {
            return usuarioDao.buscarClientes();
        } catch (DaoException e) {
            return Collections.emptyList();
        }
    }

    public List<Usuario> buscarPorFiltro(String filtro) {
        try {
            return usuarioDao.buscarPorFiltro(filtro);
        } catch (DaoException e) {
            return Collections.emptyList();
        }
    }

    private String validarUsuario(Usuario usuario) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank())
            return "El nombre es obligatorio.";
        if (usuario.getApellidos() == null || usuario.getApellidos().isBlank())
            return "Los apellidos son obligatorios.";
        if (usuario.getTelefono() == null || usuario.getTelefono().isBlank())
            return "El teléfono es obligatorio.";
        if (usuario.getTipo() == null)
            return "El tipo de usuario es obligatorio.";
        if (usuario.getTipo() == Usuario.Tipo.EMPLEADO) {
            if (usuario.getUsername() == null || usuario.getUsername().isBlank())
                return "El nombre de usuario es obligatorio para empleados.";
            if (usuario.getPassword() == null || usuario.getPassword().isBlank())
                return "La contraseña es obligatoria para empleados.";
            if (usuario.getRol() == null)
                return "El rol es obligatorio para empleados.";
        }
        return null;
    }
}