package italiapizza.controlador;

import italiapizza.dao.impl.UsuarioDaoImpl;
import italiapizza.excepcion.DaoException;
import italiapizza.excepcion.UsuarioNoEliminableException;
import italiapizza.modelo.Usuario;

import java.util.Collections;
import java.util.List;

public class UsuarioControlador {

    private final UsuarioDaoImpl usuarioDao;

    public UsuarioControlador() {
        this.usuarioDao = new UsuarioDaoImpl();
    }

    UsuarioControlador(UsuarioDaoImpl usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    public String registrar(Usuario usuario) {
        String mensajeValidacion = validarUsuario(usuario);
        if (mensajeValidacion != null) {
            return mensajeValidacion;
        }
        try {
            usuarioDao.registrar(usuario);
            return null;
        } catch (DaoException excepcion) {
            return "Error al guardar: " + excepcion.getMessage();
        }
    }

    public String actualizar(Usuario usuario) {
        String mensajeValidacion = validarUsuario(usuario);
        if (mensajeValidacion != null) {
            return mensajeValidacion;
        }
        try {
            usuarioDao.actualizar(usuario);
            return null;
        } catch (DaoException excepcion) {
            return "Error al actualizar: " + excepcion.getMessage();
        }
    }

    public String eliminar(Usuario usuario, int idUsuarioEnSesion) {
        try {
            if (usuario.getIdUsuario() == idUsuarioEnSesion) {
                throw new UsuarioNoEliminableException("No puedes eliminar tu propia cuenta.");
            }
            if (usuario.getTipo() == Usuario.Tipo.CLIENTE
                    && usuarioDao.tienePedidos(usuario.getIdUsuario())) {
                throw new UsuarioNoEliminableException(
                        "El cliente tiene pedidos registrados y no puede eliminarse.");
            }
            usuarioDao.eliminar(usuario.getIdUsuario());
            return null;
        } catch (UsuarioNoEliminableException excepcion) {
            return excepcion.getMessage();
        } catch (DaoException excepcion) {
            return "Error al eliminar: " + excepcion.getMessage();
        }
    }

    public List<Usuario> obtenerTodos() {
        try {
            return usuarioDao.buscarTodos();
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    public List<Usuario> buscarPorFiltro(String filtro) {
        try {
            return usuarioDao.buscarPorFiltro(filtro);
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    public List<Usuario> obtenerClientes() {
        try {
            return usuarioDao.buscarClientes();
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    private String validarUsuario(Usuario usuario) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            return "El nombre es requerido.";
        }
        if (usuario.getApellidos() == null || usuario.getApellidos().isBlank()) {
            return "Los apellidos son requeridos.";
        }
        if (usuario.getTelefono() == null || usuario.getTelefono().isBlank()) {
            return "El teléfono es requerido.";
        }
        if (usuario.getTipo() == null) {
            return "Selecciona el tipo de usuario.";
        }
        if (usuario.getTipo() == Usuario.Tipo.EMPLEADO) {
            if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
                return "El nombre de usuario es requerido para empleados.";
            }
            if (usuario.getIdUsuario() == 0
                    && (usuario.getPassword() == null || usuario.getPassword().isBlank())) {
                return "La contraseña es requerida para empleados.";
            }
            if (usuario.getRol() == null) {
                return "Selecciona el rol del empleado.";
            }
        }
        return null;
    }
}
